package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.standard.util.Ut;
import org.reflections.Reflections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Constructor;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);

        // 1단계: 기본 생성자 있는 빈 먼저 생성
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotation()) continue;

            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            if (constructor.getParameterTypes().length == 0) {
                try {
                    String beanName = Ut.str.lcfirst(clazz.getSimpleName());
                    Object instance = constructor.newInstance();
                    beans.put(beanName, instance);
                } catch (Exception e) {
                    throw new RuntimeException("빈 생성 실패: " + clazz.getName(), e);
                }
            }
        }

        // 2단계: 생성자 파라미터 있는 빈 생성 (1단계 빈들을 주입)
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotation()) continue;

            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            if (constructor.getParameterTypes().length > 0) {
                try {
                    String beanName = Ut.str.lcfirst(clazz.getSimpleName());
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] args = new Object[paramTypes.length];

                    for (int i = 0; i < paramTypes.length; i++) {
                        // 파라미터 타입으로 beans에서 찾기
                        String paramBeanName = Ut.str.lcfirst(paramTypes[i].getSimpleName());
                        args[i] = beans.get(paramBeanName);
                    }

                    Object instance = constructor.newInstance(args);
                    beans.put(beanName, instance);
                } catch (Exception e) {
                    throw new RuntimeException("빈 생성 실패: " + clazz.getName(), e);
                }
            }
        }
    }

    public <T> T genBean(String beanName) {
        return (T) beans.get(beanName);
    }
}