package com.github.wujiuye.transport.demo.server;

import com.github.wujiuye.transport.demo.service.HelloWordRpc;
import com.github.wujiuye.transport.demo.service.HelloWordRpcImpl;
import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.rpc.RpcMethod;

import java.lang.reflect.Method;

/**
 * @author wujiuye 2021/01/07
 */
public class DemoRpcInvokerRouter implements RpcInvokerRouter {

    private HelloWordRpc helloWordRpc = new HelloWordRpcImpl();
    private RpcMethod rpcMethod;

    {
        try {
            Method method = HelloWordRpc.class.getMethod("sayHello");
            rpcMethod = new RpcMethod(helloWordRpc, method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RpcMethod processor(Class<?> interfaces, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (interfaces == HelloWordRpc.class && methodName.equals(rpcMethod.getMethod().getName())) {
            return rpcMethod;
        }
        throw new NoSuchMethodException(interfaces.getName() + "#" + methodName);
    }

}
