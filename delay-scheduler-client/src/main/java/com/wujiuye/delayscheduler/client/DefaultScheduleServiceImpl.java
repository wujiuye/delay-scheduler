package com.wujiuye.delayscheduler.client;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.Action;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.core.rpc.ActionRpcInterface;
import com.wujiuye.delayscheduler.core.service.ScheduleService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wujiuye 2021/01/20
 */
public class DefaultScheduleServiceImpl implements ScheduleService {

    private ActionRpcInterface actionRpcInterface;
    private String application;

    public DefaultScheduleServiceImpl(String application, ActionRpcInterface actionRpcInterface) {
        this.actionRpcInterface = actionRpcInterface;
        this.application = application;
    }

    @Override
    public boolean cancelAction(long actionId) {
        return actionRpcInterface.cancel(application, actionId);
    }

    private static ActionLog newActionLog(Action<?> action) {
        ActionLog actionLog = new ActionLog();
        // 服务端生成
        actionLog.setId(-1);
        actionLog.setSubmitDate(System.currentTimeMillis() / 1000);
        actionLog.setParam(JsonUtils.toJsonString(action.getParam()));
        actionLog.setTaskName(action.getTaskName());
        actionLog.setNextPeriodIndex(0);
        return actionLog;
    }

    @Override
    public <T> long submitAction(Action<T> action, TimeUnit timeUnit, long... periods) {
        ActionLog actionLog = newActionLog(action);
        List<Long> periodArray = Arrays.stream(periods).boxed().collect(Collectors.toList());
        actionLog.setFreePeriods(periodArray);
        return actionRpcInterface.submit(application, actionLog);
    }

    @Override
    public <T> long submitAction(Action<T> action, LocalDateTime datetime) {
        ActionLog actionLog = newActionLog(action);
        actionLog.setNextPeriodIndex(0);
        actionLog.setFreePeriods(Collections.singletonList(datetime.toEpochSecond(ZoneOffset.ofHours(8))));
        return actionRpcInterface.submit(application, actionLog);
    }

}
