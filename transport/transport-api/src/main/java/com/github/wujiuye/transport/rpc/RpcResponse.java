package com.github.wujiuye.transport.rpc;

/**
 * @author wujiuye 2020/12/17
 */
public class RpcResponse {

    /**
     * 响应的结果
     */
    private Object result;
    /**
     * 响应的异常
     */
    private Throwable exception;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "result=" + result +
                ", exception=" + exception +
                '}';
    }

}
