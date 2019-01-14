package com.kevin.designpattern.proxy;

import java.lang.reflect.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kevin-Tu on 2018/6/14.
 */
public class Test {

    public static void main(String[] args) {
        /*Student student = new Student();
        MyInvocationHandler handler = new MyInvocationHandler(student);
        Person person = (Person) Proxy.newProxyInstance(Student.class.getClassLoader(), Student.class.getInterfaces(), handler);
        person.sayHello();*/



        String str = "123";

        Pattern pattern = Pattern.compile("^([0-9]{3}|[A-Za-z]{3})([^ ]*)$");

        Matcher isNum = pattern.matcher(str);
        System.out.println("result: "+ isNum.matches());
    }
}
