package com.kevin.designpattern.proxy;

import java.lang.reflect.Proxy;

/**
 * Kevin-Tu on 2018/6/14.
 */
public class Test {

    public static void main(String[] args) {
        Student student = new Student();
        MyInvocationHandler handler = new MyInvocationHandler(student);
        Person person = (Person) Proxy.newProxyInstance(Student.class.getClassLoader(), Student.class.getInterfaces(), handler);
        person.sayHello();
    }
}
