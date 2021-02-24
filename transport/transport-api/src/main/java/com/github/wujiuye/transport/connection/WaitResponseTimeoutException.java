package com.github.wujiuye.transport.connection;

/**
 * 等待超时异常
 *
 * @author wujiuye 2021/02/02
 */
public class WaitResponseTimeoutException extends TransportIOException {

    public WaitResponseTimeoutException(String message) {
        super(message);
    }

}
