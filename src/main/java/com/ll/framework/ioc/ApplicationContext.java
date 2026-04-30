package com.ll.framework.ioc;



import com.ll.framework.ioc.annotations.Component;
import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;
import org.reflections.Reflections;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Object> beanMap;
    private final Set<Class<?>> beanClasses;

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
        this.beanMap = new HashMap<>();
        this.beanClasses = new HashSet<>();
    }

    public void init() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> component = reflections.getTypesAnnotatedWith(Component.class);

        for(Class<?> clazz : component) {
            if(clazz.isAnnotation()) continue;
            beanClasses.add(clazz);
        }
        for(Class<?> clazz : beanClasses) {
            createBean(clazz);
        }
    }

    public String getBeanName(Class<?> clazz) {
        return Introspector.decapitalize(clazz.getSimpleName());

    }

    public Object createBean(Class<?> clazz) {
        String beanName = getBeanName(clazz);

        if (beanMap.containsKey(beanName)) {
            return beanMap.get(beanName);
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                String paramBeanName = getBeanName(paramTypes[i]);

                if (beanMap.containsKey(paramBeanName)) {
                    args[i] = beanMap.get(paramBeanName);
                } else {
                    args[i] = createBean(paramTypes[i]);
                }
            }

            Object bean = constructor.newInstance(args);
            beanMap.put(beanName, bean);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public <T> T genBean(String beanName) {
        return (T) beanMap.get(beanName);
    }
}
