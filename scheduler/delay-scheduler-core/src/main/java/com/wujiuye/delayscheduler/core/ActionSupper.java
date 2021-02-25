package com.wujiuye.delayscheduler.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 调度任务
 *
 * @author wujiuye 2020/08/24
 * @see Action
 * @see ActionAdapter
 */
public abstract class ActionSupper<T> implements Action<T> {

    private final String typeName;

    protected ActionSupper() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new UnsupportedOperationException("类型异常！");
        } else {
            this.typeName = ((ParameterizedType) superClass).getActualTypeArguments()[0].getTypeName();
        }
        if (typeName.equals(Object.class.getName())) {
            throw new UnsupportedOperationException("您必须指定参数类型，无果不需要参数，可指定参数类型为Void");
        }
    }

    @Override
    public Class<T> getParamType() {
        try {
            return (Class<T>) Class.forName(this.typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class load fail." + e.getMessage());
        }
    }

}
