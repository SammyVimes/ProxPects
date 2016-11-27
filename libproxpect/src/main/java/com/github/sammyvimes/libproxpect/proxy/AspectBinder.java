package com.github.sammyvimes.libproxpect.proxy;

import android.util.Pair;

import com.github.sammyvimes.libproxpect.ReflectionHelper;
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

    private static final List<Class<? extends Annotation>> aspects = new LinkedList<>();
    private static final Map<Class, RootInvocationHandler> handlersMap = new HashMap<>();

    public static <INTERFACE, REAL_TYPE extends INTERFACE> INTERFACE process(final REAL_TYPE object,
                                                                             final Class<INTERFACE> interfaceClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class objectClass = object.getClass();
        // получение списка всех методов интерфейса
        Method[] declaredMethods = interfaceClass.getDeclaredMethods();
        // возможно этот класс уже обработан, достаём invocation handler из кэша
        RootInvocationHandler rootHandler = handlersMap.get(objectClass);
        if (rootHandler == null) {
            Map<RootInvocationHandler.MethodWrapper, ChainedInvocationHandler> methodHandlerMap = new HashMap<>();
            for (Method method : declaredMethods) {
                // попытка создать хендлер для метода
                ChainedInvocationHandler methodHandler = processMethod(method);
                // достаём переопределённый метод из наследника
                Method overriddenMethod = ReflectionHelper.getOverriddenMethod(method, object);
                if (overriddenMethod != null) {
                    // есть переопределённый метод в наследнике, попробуем создать хендлер для него
                    ChainedInvocationHandler overriddenMethodHandler = processMethod(overriddenMethod);
                    if (overriddenMethodHandler != null) {
                        if (methodHandler != null) {
                            // есть хендлер для метода из интерфейса, проставим хендлер переопределенного
                            // в конец
                            methodHandler.getBottomHandler().setNestedHandler(overriddenMethodHandler);
                        } else {
                            // это единственный хендлер, добавим его в хендлеры
                            methodHandlerMap.put(new RootInvocationHandler.MethodWrapper(method), overriddenMethodHandler);
                        }
                    } else {
                        // нет хендлера переопределенного метода, сохраняем хендлер из парента
                        methodHandlerMap.put(new RootInvocationHandler.MethodWrapper(method), methodHandler);
                    }
                } else if (methodHandler != null) {
                    //  нет переопределенного метода, сохраняем хендлер из парента
                    methodHandlerMap.put(new RootInvocationHandler.MethodWrapper(method), methodHandler);
                }
            }
            // создаём хендлер для всех объектов класса REAL_TYPE
            rootHandler = new RootInvocationHandler(methodHandlerMap);
            handlersMap.put(objectClass, rootHandler);
        }
        // создаём прокси объект с пошаренным invocation handler'ом
        return (INTERFACE) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass}, new SharedInvocationHandler(object, rootHandler));
    }

    private static ChainedInvocationHandler processMethod(final Method method) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Pair<AnnotationAndClass, Aspect>> aspectAnnotations = new LinkedList<>();
        // достанем все аннотации метода
        Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
        for (Annotation annotation : declaredAnnotations) {
            // попробуем вытащить аспектную аннотацию
            Pair<AnnotationAndClass, Aspect> aspect = getAspect(annotation);
            if (aspect != null) {
                aspectAnnotations.add(aspect);
            }
        }
        if (aspectAnnotations.size() > 0) {
            ChainedInvocationHandler methodHandler = null;
            // начнём с последней найденной аннотации
            Collections.reverse(aspectAnnotations);
            for (Pair<AnnotationAndClass, Aspect> aspectAnnotation : aspectAnnotations) {
                Aspect aspect = aspectAnnotation.second;
                AnnotationAndClass annotationAndClass = aspectAnnotation.first;
                // класс обработчика вызова метода
                Class<?> aspectHandler = aspect.value();
                // тут значение аннотации
                Annotation annotation = annotationAndClass.annotation;
                // а это нужно потому что annotation не сохраняет реального класса в рантайме, там прокси
                Class annotationClass = annotationAndClass.aClass;
                // создадим обработчик метода (аргумент конструктора всегда значение аннотации, а тип -- класс аннотации)
                ChainedInvocationHandler handler = (ChainedInvocationHandler) aspectHandler.getConstructor(annotationClass).newInstance(annotation);
                handler.setNestedHandler(methodHandler);
                methodHandler = handler;
            }
            return methodHandler;
        }
        return null;
    }

    @SafeVarargs
    public static void registerAspects(final Class<? extends Annotation>... annotations) {
        Collections.addAll(aspects, annotations);
    }

    /**
     * Получение аспекта по аннотации, если аннотация аспектная
     *
     * @param annotation аннотация
     * @return пара пары аннотация (конкретное значение) и класса аннотации и значения аннотации Aspect
     */
    private static Pair<AnnotationAndClass, Aspect> getAspect(final Annotation annotation) {
        // пройдём по всем зарегистрированным классам аннотаций
        // (из-за того что Annotation в рантайме прокси и конкретный класс иначе не получить)
        for (int i = 0; i < aspects.size(); i++) {
            Class<?> aClass = aspects.get(i);
            // для прокси это тоже вернёт true
            // getClass, getDeclaredAnnotations не сработает, т.к. getClass вернёт smth like Proxy#01
            if (aClass.isInstance(annotation)) {
                Annotation[] declaredAnnotations = aClass.getDeclaredAnnotations();
                // теперь найдём аннотацию @Aspect
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

        AnnotationAndClass(final Annotation annotation, final Class aClass) {
            this.annotation = annotation;
            this.aClass = aClass;
        }
    }

}
