package com.wujiuye.delayscheduler.core;

/**
 * Action适配器
 *
 * @param <T>
 */
public abstract class ActionAdapter<T> implements Action<T> {

    private Action<T> action;

    public ActionAdapter(Action<T> action) {
        this.action = action;
    }

    @Override
    public Class<T> getParamType() {
        return action.getParamType();
    }

    @Override
    public String getTaskName() {
        return action.getTaskName();
    }

    @Override
    public boolean execute(int curPeriod, T param) {
        return action.execute(curPeriod, param);
    }

}