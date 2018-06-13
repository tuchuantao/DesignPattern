package com.kevin.designpattern.proxy

import java.lang.reflect.Proxy

/**
 * Kevin-Tu on 2018/6/13.
 */

    fun main(args: Array<String>) {
        var student = Student()

        // 获得加载被代理类的 类加载器
        var loader = Thread.currentThread().contextClassLoader
        // 指明被代理类实现的接口
        var interfaces = student.javaClass.interfaces
        // 创建被代理类的委托类,之后想要调用被代理类的方法时，都会委托给这个类的invoke(Object proxy, Method method, Object[] args)方法
        var handler = MyInvocationHandler(student)
        // 生成代理类
        var proxy = Proxy.newProxyInstance(loader, interfaces, handler) as Person
        proxy.sayHello()
    }
