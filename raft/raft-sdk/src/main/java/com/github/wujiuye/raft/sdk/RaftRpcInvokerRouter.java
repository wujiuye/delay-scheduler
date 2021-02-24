package com.github.wujiuye.raft.sdk;

import com.github.wujiuye.raft.rpc.replication.AppendEntriesRpc;
import com.github.wujiuye.raft.rpc.vote.RequestVoteRpc;
import com.github.wujiuye.transport.rpc.RpcInvokerRouter;
import com.github.wujiuye.transport.rpc.RpcMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wujiuye 2020/12/17
 */
public class RaftRpcInvokerRouter implements RpcInvokerRouter {

    private Map<Class<?>, Object> objectMap = new HashMap<>();
    private Map<String, RpcMethod> processorMap = new HashMap<>();

    public RaftRpcInvokerRouter(AppendEntriesRpc appendEntriesRpc, RequestVoteRpc requestVoteRpc) {
        objectMap.put(AppendEntriesRpc.class, appendEntriesRpc);
        objectMap.put(RequestVoteRpc.class, requestVoteRpc);
        this.init(AppendEntriesRpc.class, RequestVoteRpc.class);
    }

    private static String getMethodKey(Class<?> interfaces, String methodName, Class<?>[] parameterTypes) {
        StringBuilder builder = new StringBuilder();
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (Class<?> pt : parameterTypes) {
                builder.append(pt.getName()).append("@");
            }
        }
        return interfaces.getName() + "@" + methodName + "@" + builder.toString();
    }

    private void init(Class<?>... interfacess) {
        for (Class<?> cla : interfacess) {
            Method[] methods = cla.getMethods();
            for (Method method : methods) {
                RpcMethod rpcMethod = new RpcMethod(objectMap.get(cla), method);
                processorMap.put(getMethodKey(cla, method.getName(), method.getParameterTypes()), rpcMethod);
            }
        }
    }

    @Override
    public RpcMethod processor(Class<?> interfaces, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        String processor = getMethodKey(interfaces, methodName, parameterTypes);
        RpcMethod rpcMethod = processorMap.get(processor);
        if (rpcMethod == null) {
            throw new NoSuchMethodException(processor + " not found.");
        }
        return rpcMethod;
    }

}
