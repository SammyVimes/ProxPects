package com.github.sammyvimes.libproxpect.proxy;

import android.util.Pair;

import com.github.sammyvimes.libproxpect.annotation.Aspect;
import com.github.sammyvimes.libproxpect.aspect.ChainedInvocationHandler;
import com.github.sammyvimes.libproxpect.aspect.RootInvocationHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Semyon on 26.02.2016.
 */
public class AspectBinder {

    private static final List<Class<?>> aspects = new LinkedList<>();
    private static final Map<Class, RootInvocationHandler> handlersMap = new HashMap<>();

    public static <INTERFACE, REAL_TYPE extends INTERFACE> INTERFACE process(final REAL_TYPE object,
                                                                             final Class<INTERFACE> interfaceClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class objectClass = object.getClass();
        Method[] declaredMethods = interfaceClass.getDeclaredMethods();
        RootInvocationHandler rootHandler = handlersMap.get(objectClass); //TODO: put root into map
        if (rootHandler == null) {
            Map<RootInvocationHandler.MethodWrapper, ChainedInvocationHandler> methodHandlerMap = new HashMap<>();
            for (Method method : declaredMethods) {
                List<Pair<AnnotationAndClass, Aspect>> aspectAnnotations = new LinkedList<>();
                Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    Pair<AnnotationAndClass, Aspect> aspect = getAspect(annotation);
                    if (aspect != null) {
                        aspectAnnotations.add(aspect);
                    }
                }
                if (aspectAnnotations.size() > 0) {
                    ChainedInvocationHandler methodHandler = null;
                    Collections.reverse(aspectAnnotations);
                    for (Pair<AnnotationAndClass, Aspect> aspectAnnotation : aspectAnnotations) {
                        Aspect aspect = aspectAnnotation.second;
                        AnnotationAndClass annotationAndClass = aspectAnnotation.first;
                        Class<?> aspectHandler = aspect.value();
                        Annotation annotation = annotationAndClass.annotation;
                        Class annotationClass = annotationAndClass.aClass;
                        ChainedInvocationHandler handler = (ChainedInvocationHandler) aspectHandler.getConstructor(annotationClass).newInstance(annotation);
                        handler.setNestedHandler(methodHandler);
                        methodHandler = handler;
                    }
                    methodHandlerMap.put(new RootInvocationHandler.MethodWrapper(method), methodHandler);
                }
            }
            rootHandler = new RootInvocationHandler(methodHandlerMap);
            handlersMap.put(objectClass, rootHandler);
        }
        return (INTERFACE) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass}, new SharedInvocationHandler(object, rootHandler));
    }

    public static void registerAspects(final Class<? extends Annotation>... annotations) {
        Collections.addAll(aspects, annotations);
    }

    private static Pair<AnnotationAndClass, Aspect> getAspect(final Annotation annotation) {
        for (int i = 0; i < aspects.size(); i++) {
            Class<?> aClass = aspects.get(i);
            if (aClass.isInstance(annotation)) {
                Annotation[] declaredAnnotations = aClass.getDeclaredAnnotations();
                for (Annotation aAnnotation : declaredAnnotations) {
                    if (aAnnotation instanceof Aspect) {
                        return new Pair<>(new AnnotationAndClass(annotation, aClass), (Aspect) aAnnotation);
                    }
                }
            }
        }
        return null;
    }

    private static class AnnotationAndClass {

        Annotation annotation;

        Class aClass;

        public AnnotationAndClass(final Annotation annotation, final Class aClass) {
            this.annotation = annotation;
            this.aClass = aClass;
        }
    }

}
