package com.github.wujiuye.transport.connection;

/**
 * I/O异常
 *
 * @author wujiuye 2020/10/12
 */
public abstract class TransportIOException extends Exception {

    private String message;

    public TransportIOException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

}
