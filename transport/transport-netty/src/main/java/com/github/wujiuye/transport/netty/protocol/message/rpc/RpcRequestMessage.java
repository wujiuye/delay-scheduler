package com.github.wujiuye.transport.netty.protocol.message.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.github.wujiuye.transport.rpc.RpcRequest;
import com.github.wujiuye.transport.netty.protocol.message.Message;
import com.github.wujiuye.transport.netty.protocol.message.MessageTypes;
import com.github.wujiuye.transport.netty.protocol.message.MessageVersions;
import io.netty.util.internal.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC请求消息
 *
 * @author wujiuye 2020/12/17
 */
public class RpcRequestMessage implements Message {

    private String transactionId;
    private Map<String, String> body;
    @JsonIgnore
    private transient RpcRequest rpcRequest;

    public RpcRequestMessage() {

    }

    public RpcRequestMessage(RpcRequest rpcRequest) {
        setRpcRequest(rpcRequest);
    }

    @Override
    public Byte getType() {
        return MessageTypes.MSG_TYPE_RPC_REQUEST;
    }

    @Override
    public Byte getVersion() {
        return MessageVersions.VERSION;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setRpcRequest(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
        this.body = new HashMap<>();
        if (rpcRequest.getInterfaces() != null) {
            this.body.put("interfaces", rpcRequest.getInterfaces().getName());
        }
        if (rpcRequest.getParameterTypes() != null && rpcRequest.getParameterTypes().length > 0) {
            String[] parameterTypesStr = new String[rpcRequest.getParameterTypes().length];
            for (int i = 0; i < rpcRequest.getParameterTypes().length; i++) {
                parameterTypesStr[i] = rpcRequest.getParameterTypes()[i].getName();
            }
            this.body.put("parameterTypes", JsonUtils.toJsonString(parameterTypesStr));
        }
        this.body.put("methodName", rpcRequest.getMethodName());
        if (rpcRequest.getArguments() != null && rpcRequest.getArguments().length > 0) {
            String[] arguments = new String[rpcRequest.getArguments().length];
            for (int i = 0; i < rpcRequest.getArguments().length; i++) {
                arguments[i] = JsonUtils.toJsonString(rpcRequest.getArguments()[i]);
            }
            this.body.put("arguments", JsonUtils.toJsonString(arguments));
        }
    }

    public RpcRequest getRpcRequest() {
        if (rpcRequest == null) {
            RpcRequest rpcRequest = new RpcRequest();
            try {
                rpcRequest.setInterfaces(Class.forName(this.body.get("interfaces")));
                rpcRequest.setMethodName(this.body.get("methodName"));
                String types = this.body.getOrDefault("parameterTypes", null);
                String values = this.body.getOrDefault("arguments", null);
                if (!StringUtil.isNullOrEmpty(types)) {
                    String[] typeStrs = JsonUtils.fromJsonList(types, String.class).toArray(new String[0]);
                    List<String> valueList = JsonUtils.fromJsonList(values, String.class);
                    String[] valueStrs = valueList == null ? new String[0] : valueList.toArray(new String[0]);
                    Class<?>[] classs = new Class<?>[typeStrs.length];
                    Object[] arguments = new Object[typeStrs.length];
                    for (int index = 0; index < typeStrs.length; index++) {
                        Class<?> aClass = Class.forName(typeStrs[index]);
                        classs[index] = aClass;
                        if (index < valueStrs.length && !StringUtil.isNullOrEmpty(valueStrs[index])) {
                            arguments[index] = JsonUtils.fromJson(valueStrs[index], aClass);
                        } else {
                            arguments[index] = null;
                        }
                    }
                    rpcRequest.setParameterTypes(classs);
                    rpcRequest.setArguments(arguments);
                }
                this.rpcRequest = rpcRequest;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getClass().getName() + ":" + e.getMessage() + "[" + JsonUtils.toJsonString(body) + "]");
            }
        }
        return rpcRequest;
    }

}
