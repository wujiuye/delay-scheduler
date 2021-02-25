package com.wujiuye.delayscheduler.client;

import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import org.junit.Test;

import java.util.List;

public class RpcDecodeTest {

    @Test
    public void testDecodeList() {
        String json = "[{\"id\":58060066603081728,\"taskName\":\"testAction\",\"param\":\"xxxxx\",\"submitDate\":1612169002,\"freePeriods\":[2,4,8],\"nextPeriodIndex\":0,\"status\":\"pending\"}]";
        List<ActionLog> actionLogList = JsonUtils.fromJsonList(json, ActionLog.class);
        System.out.println(JsonUtils.toJsonString(actionLogList));
    }

}
