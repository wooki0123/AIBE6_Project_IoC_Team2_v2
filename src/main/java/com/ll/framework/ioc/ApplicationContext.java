package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(reflections.getTypesAnnotatedWith(Service.class));
        classes.addAll(reflections.getTypesAnnotatedWith(Repository.class));

        System.out.println("classes = " + classes);
        try {
            for (Class<?> beanClass : classes) {
                if (beanClass.isInterface() || beanClass.isAnnotation()) continue;
                Constructor<?> constructor = beanClass.getDeclaredConstructors()[0];
                if (constructor.getParameterTypes().length == 0) {
                    beans.put(toBeanName(beanClass), constructor.newInstance());
                }
            }
            for (Class<?> beanClass : classes) {
                if (beanClass.isInterface() || beanClass.isAnnotation()) continue;
                Constructor<?> constructor = beanClass.getDeclaredConstructors()[0];
                if (constructor.getParameterTypes().length > 0) {
                    Object[] params = new Object[constructor.getParameterTypes().length];
                    for (int i = 0; i < params.length; i++) {
                        params[i] = beans.get(toBeanName(constructor.getParameterTypes()[i]));
                    }
                    beans.put(toBeanName(beanClass), constructor.newInstance(params));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toBeanName(Class<?> beanClass) {
        return beanClass.getSimpleName().substring(0, 1).toLowerCase() + beanClass.getSimpleName().substring(1);
    }

    public void init() {

    }

    public <T> T genBean(String beanName) {
        System.out.println("beans = " + beans);
        System.out.println("beanName = " + beanName);
        return (T) beans.get(beanName);
    }
}
