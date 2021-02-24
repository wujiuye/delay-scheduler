package com.github.wujiuye.transport.rpc;

import java.util.Arrays;

/**
 * @author wujiuye 2020/12/17
 */
public class RpcRequest {

    /**
     * 请求的接口
     */
    private Class<?> interfaces;
    /**
     * 请求的方法
     */
    private String methodName;
    /**
     * 请求的参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数
     */
    private Object[] arguments;

    public Class<?> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Class<?> interfaces) {
        this.interfaces = interfaces;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaces=" + interfaces +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }

}
