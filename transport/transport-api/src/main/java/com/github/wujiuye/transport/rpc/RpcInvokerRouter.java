package com.github.wujiuye.transport.rpc;

/**
 * @author wujiuye 2020/12/17
 */
public interface RpcInvokerRouter {

    /**
     * 获取处理器
     *
     * @param interfaces     接口
     * @param methodName     方法
     * @param parameterTypes 参数类型
     * @return
     */
    RpcMethod processor(Class<?> interfaces, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException;

}
