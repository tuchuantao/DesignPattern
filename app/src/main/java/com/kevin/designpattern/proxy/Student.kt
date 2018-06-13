package com.kevin.designpattern.proxy

/**
 * Kevin-Tu on 2018/6/13.
 */
class Student: Person {

    override fun sayHello() {
        android.util.Log.d("kevin", "Student say Hello World!")
    }
}
