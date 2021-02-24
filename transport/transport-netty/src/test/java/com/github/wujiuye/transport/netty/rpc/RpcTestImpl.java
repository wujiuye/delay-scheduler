package com.github.wujiuye.transport.netty.rpc;

import java.util.HashMap;
import java.util.Map;

public class RpcTestImpl implements RpcTest {

    @Override
    public void sayHello() {
        System.out.println("RpcTestImpl#sayHello");
    }

    @Override
    public Map<String, Object> sayHello(String name) {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("sxx", "xxxx");
        return hashMap;
    }

    @Override
    public Integer inc(Integer i1, Integer i2) {
        return i1 + i2;
    }

}
