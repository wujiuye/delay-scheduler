package com.github.wujiuye.transport.netty.protocol.message.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.github.wujiuye.transport.netty.protocol.serialize.JdkSerializer;
import com.github.wujiuye.transport.rpc.RpcResponse;
import com.github.wujiuye.transport.netty.protocol.message.Message;
import com.github.wujiuye.transport.netty.protocol.message.MessageTypes;
import com.github.wujiuye.transport.netty.protocol.message.MessageVersions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC响应消息
 *
 * @author wujiuye 2020/12/17
 */
public class RpcResponseMessage implements Message {

    private String transactionId;
    private Map<String, String> body;
    @JsonIgnore
    private transient RpcResponse rpcResponse;
    private static transient JdkSerializer jdkSerializer = new JdkSerializer();

    public RpcResponseMessage() {
    }

    public RpcResponseMessage(RpcResponse rpcResponse) {
        this.setRpcResponse(rpcResponse);
    }

    @Override
    public Byte getType() {
        return MessageTypes.MSG_TYPE_RPC_RESPONSE;
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

    private static Throwable getCause(Throwable throwable) {
        Throwable cause = throwable;
        for (; cause.getCause() != null; cause = cause.getCause()) {
        }
        return cause;
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        this.body = new HashMap<>();
        if (rpcResponse.getResult() != null) {
            this.body.put("result", JsonUtils.toJsonString(rpcResponse.getResult()));
            String resultType = rpcResponse.getResult().getClass().getName();
            if (rpcResponse.getResult() instanceof List
                    && ((List<?>) rpcResponse.getResult()).size() > 0) {
                resultType = resultType + ";" + ((List<?>) rpcResponse.getResult()).get(0).getClass().getName();
            }
            this.body.put("resultType", resultType);
        }
        if (rpcResponse.getException() != null) {
            Throwable cause = getCause(rpcResponse.getException());
            this.body.put("error", new String(jdkSerializer.serialize(cause), StandardCharsets.ISO_8859_1));
            this.body.put("errorType", cause.getClass().getName());
        }
    }

    public RpcResponse getRpcResponse() {
        if (rpcResponse == null && this.body != null) {
            rpcResponse = new RpcResponse();
            try {
                if (this.body.containsKey("result")) {
                    String resultType = this.body.get("resultType");
                    Class<?> tClass, eClass = null;
                    if (!resultType.contains(";")) {
                        tClass = Class.forName(resultType);
                    } else {
                        String[] resultTypes = resultType.split(";");
                        tClass = Class.forName(resultTypes[0]);
                        eClass = Class.forName(resultTypes[1]);
                    }
                    Object result;
                    if (eClass == null) {
                        result = JsonUtils.fromJson(this.body.get("result"), tClass);
                    } else {
                        result = JsonUtils.fromJsonList(this.body.get("result"), eClass);
                    }
                    rpcResponse.setResult(result);
                }
                if (this.body.containsKey("error")) {
                    Class<Exception> exceptionClass = (Class<Exception>) Class.forName(this.body.get("errorType"));
                    rpcResponse.setException(jdkSerializer.deserialize(exceptionClass,
                            this.body.get("error").getBytes(StandardCharsets.ISO_8859_1)));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return rpcResponse;
    }

}
