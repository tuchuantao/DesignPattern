package com.kevin.designpattern.proxy;

/**
 * Kevin-Tu on 2018/6/14.
 */
public class Student implements Person {
    @Override
    public void sayHello() {
        System.out.println("Student say Hello World");
    }
}
