package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

public class ApplicationContext {
    private String basePackage;
    private Map<String, Object> beanMap = new HashMap<>();
    private Map<String, Class<?>> beanClassMap = new HashMap<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        registerBeanClasses();

        for (String beanName : beanClassMap.keySet()) {
            createBean(beanName);
        }
    }

    public <T> T genBean(String beanName) {
        return (T) beanMap.get(beanName);
    }

    private List<Class<?>> scanClasses() {
        List<Class<?>> classes = new ArrayList<>();

        String path = basePackage.replace(".", "/");
        URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource(path);

        if (resource == null) return classes;

        File dir = new File(resource.getFile());
        scanDir(dir, basePackage, classes);

        return classes;
    }

    private void scanDir(File dir, String packageName, List<Class<?>> classes) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDir(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");

                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void registerBeanClasses() {
        for (Class<?> clazz : scanClasses()) {
            if (isComponent(clazz)) {
                String beanName = lowerFirst(clazz.getSimpleName());
                beanClassMap.put(beanName, clazz);
            }
        }
    }

    private boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class)
                || clazz.isAnnotationPresent(Service.class)
                || clazz.isAnnotationPresent(Repository.class);
    }

    private Object createBean(String beanName) {
        if (beanMap.containsKey(beanName)) {
            return beanMap.get(beanName);
        }

        Class<?> clazz = beanClassMap.get(beanName);

        try {
            Constructor<?> constructor = clazz.getConstructors()[0];

            Object[] args = Arrays.stream(constructor.getParameterTypes())
                    .map(paramType -> {
                        String depName = lowerFirst(paramType.getSimpleName());
                        return createBean(depName);
                    })
                    .toArray();

            Object instance = constructor.newInstance(args);

            beanMap.put(beanName, instance);

            return instance;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String lowerFirst(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}