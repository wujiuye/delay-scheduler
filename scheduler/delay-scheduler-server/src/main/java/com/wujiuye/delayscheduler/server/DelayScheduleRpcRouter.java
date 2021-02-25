package com.wujiuye.delayscheduler.server;

import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.rpc.RpcMethod;
import com.wujiuye.delayscheduler.core.rpc.ActionRpcInterface;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 延迟调度RPC路由器
 *
 * @author wujiuye 2021/01/12
 */
public class DelayScheduleRpcRouter implements RpcInvokerRouter {

    private final ActionRpcInterface actionRpcInterface;
    private final Map<String, RpcMethod> methodMap;

    public DelayScheduleRpcRouter(ActionRpcInterface actionRpcInterface) {
        this.actionRpcInterface = actionRpcInterface;
        this.methodMap = getAllRpcMethods();
    }

    private Map<String, RpcMethod> getAllRpcMethods() {
        Map<String, RpcMethod> methodMap = new HashMap<>();
        Method[] methods = this.actionRpcInterface.getClass().getMethods();
        for (Method method : methods) {
            String key = getKey(method.getName(), method.getParameterTypes());
            methodMap.put(key, new RpcMethod(this.actionRpcInterface, method));
        }
        return methodMap;
    }

    private static String getKey(String methodName, Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return methodName;
        }
        StringBuilder sb = new StringBuilder(methodName);
        for (Class<?> pType : parameterTypes) {
            sb.append("@").append(pType.getName());
        }
        return sb.toString();
    }

    @Override
    public RpcMethod processor(Class<?> interfaces, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        String key = getKey(methodName, parameterTypes);
        RpcMethod rpcMethod = methodMap.get(key);
        if (rpcMethod == null) {
            throw new NoSuchMethodException("rpc method " + key + " not found.");
        }
        return rpcMethod;
    }

}
