package com.kevin.designpattern.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Kevin-Tu on 2018/6/13.
 */
class MyInvocationHandler(var proxy: Any): InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        android.util.Log.d("kevin", "MyInvocationHandler invoke begin")
        android.util.Log.d("kevin", proxy.javaClass.name)
        android.util.Log.d("kevin",  method.name)
        method.invoke(this.proxy, args)
        return null
    }
}