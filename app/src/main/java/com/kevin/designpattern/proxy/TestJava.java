package com.kevin.designpattern.proxy;

import java.lang.reflect.Proxy;

/**
 * Kevin-Tu on 2018/6/13.
 */
public class TestJava {

    public static void main(String[] args) {
        //TestKt.main(args);

        Student student = new Student();

        // 获得加载被代理类的 类加载器
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        // 指明被代理类实现的接口
        Class<?>[] interfaces = student.getClass().getInterfaces();
        // 创建被代理类的委托类,之后想要调用被代理类的方法时，都会委托给这个类的invoke(Object proxy, Method method, Object[] args)方法
        MyInvocationHandler handler = new MyInvocationHandler(student);
        // 生成代理类
        Person proxy = (Person) Proxy.newProxyInstance(loader, interfaces, handler);
        proxy.sayHello();
    }
}
