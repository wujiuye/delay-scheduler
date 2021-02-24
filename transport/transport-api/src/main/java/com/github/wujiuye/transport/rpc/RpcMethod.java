package com.github.wujiuye.transport.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法封装
 *
 * @author wujiuye 2020/12/17
 */
public class RpcMethod {

    private Object obj;
    private Method method;

    public RpcMethod(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Object getObj() {
        return obj;
    }

    public Object invoke(Object... params) {
        try {
            return this.method.invoke(this.obj, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
