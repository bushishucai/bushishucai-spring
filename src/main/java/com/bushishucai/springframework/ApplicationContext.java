package com.bushishucai.springframework;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();


    public ApplicationContext(Class configClass) {
        //扫描：解析配置路径，生成BeanDefinition对象，存入beanDefinitionMap，这一步不会真正的创建对象
        scan(configClass);

        //创建非懒加载且单例的bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (!beanDefinition.getLazy() && "singleton".equals(beanDefinition.getScope())) {
                if (!singletonObjects.containsKey(beanName)) {
                    Object instance = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, instance);
                }
            }
        }
    }

    //创建bean
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class type = beanDefinition.getType();
        Object instance = null;
        try {
            instance = type.newInstance(); //实例化

            //依赖注入
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(instance, getBean(field.getType()));
            }

            //处理Aware接口
            // 回调机制，将spring内部对象暴露给某个实现了特定Aware接口的bean
            if (instance instanceof ApplicationContextAware) {
                ((ApplicationContextAware) instance).setApplicationContextAware(this);
            }

            //AOP
            if (type.isAnnotationPresent(Transactional.class)) {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(type);
                Object target = instance;
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                        System.out.println("开启事务");
                        Object res = null;
                        boolean flag = true;
                        try {
                            res = method.invoke(target, objects); //userService test 执行被代理对象的方法
                        } catch (Exception e) {
                            flag = false;
                            System.out.println("回滚事务");
                        }
                        if (flag) {
                            System.out.println("提交事务");
                        }
                        return res;
                    }
                });
                instance = enhancer.create();
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * 创建spring容器，接收到一个配置类，根据ComponentScan注解去获取扫描路径
     * 通过ClassLoader，根据刚刚获取的扫描路径，获取编译目录对应的文件夹
     * 遍历文件夹下的class文件，得到.class文件，再得到绝对路径》相对路径
     * 通过ClassLoader加载对应的类，得到Class对象
     * 判断Class对象上是否有Component注解，如果有的话，这个类就是一个Bean
     * 新建BeanDefinition对象，通过Bean类上的注解，设置beanDefinition属性，最终存到beanDefinitionMap
     *
     * @param configClass
     */
    private void scan(Class configClass) {
        ComponentScan annotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = annotation.value().replace(".", "/");
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource(path);

        File fileDir = new File(resource.getFile());
        if (fileDir.isDirectory()) {
            for (File file : fileDir.listFiles()) {
                try {
                    String absolutePath = file.getAbsolutePath();
                    String classPath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                    classPath = classPath.replace("\\", ".");
                    Class<?> aClass = classLoader.loadClass(classPath);

                    if (aClass.isAnnotationPresent(Component.class)) {
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setType(aClass);
                        if (aClass.isAnnotationPresent(Scope.class)) {
                            beanDefinition.setScope(aClass.getAnnotation(Scope.class).value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }
                        if (aClass.isAnnotationPresent(Lazy.class)) {
                            beanDefinition.setLazy(true);
                        } else {
                            beanDefinition.setLazy(false);
                        }
                        String beanName = Introspector.decapitalize(aClass.getSimpleName());
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //根据名称
    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new RuntimeException();
        }

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if ("singleton".equals(beanDefinition.getScope())) {
            Object o = singletonObjects.get(beanName);
            if (null == o) {
                Object instance = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, instance);
                return instance;
            } else {
                return o;
            }
        } else if ("prototype".equals(beanDefinition.getScope())) {
            return createBean(beanName, beanDefinition);
        } else {
            throw new RuntimeException();
        }
    }


    //根据类型
    public Object getBean(Class type) {
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (type.equals(beanDefinition.getType())) {
                return getBean(beanName);
            }
        }
        return null;
    }
}
