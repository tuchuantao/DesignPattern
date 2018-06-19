package com.kevin.designpattern.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Kevin-Tu on 2018/6/14.
 */
public class MyInvocationHandler implements InvocationHandler {

    private Object target;

    public MyInvocationHandler(Object object) {
        target = object;
    }

    /**
     *
     * @param proxy 是代理类，不是被代理类
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }


/*
    public final class ProxyImpl extends Proxy implements ProxyInf {
        private static Method m3;
        private static Method m1;
        private static Method m0;
        private static Method m2;

        public ProxyImpl(InvocationHandler paramInvocationHandler)
                throws {
            super(paramInvocationHandler);
        }

        public final void say()
                throws {
            try {
                this.h.invoke(this, m3, null);
                return;
            } catch (RuntimeException localRuntimeException) {
                throw localRuntimeException;
            } catch (Throwable localThrowable) {
            }
            throw new UndeclaredThrowableException(localThrowable);
        }
     ...
    }*/
}
