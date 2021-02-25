package com.wujiuye.delayscheduler.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供两个绑定方式
 * 一：直接绑定类型，使用反射创建实例；
 * 二：直接绑定实例，使用适配器ActionAdapter创建新实例
 *
 * @author wujiuye 2021/01/11
 */
public class ActionBindManager {

    private static Map<String, Class<? extends Action<?>>> ACTION_MAP = new HashMap<>();
    private static Map<String, Action<?>> ACTION_OBJ_MAP = new HashMap<>();

    private final static Object LOCK = new Object();

    private static String getKey(String application, String taskName) {
        return application + ":" + taskName;
    }

    public static void bindAction(String application, String taskName, Class<? extends Action<?>> actionClass) {
        String key = getKey(application, taskName);
        synchronized (LOCK) {
            if (ACTION_MAP.containsKey(key)) {
                ACTION_MAP.replace(key, actionClass);
            } else {
                Map<String, Class<? extends Action<?>>> newMap = new HashMap<>(ACTION_MAP);
                newMap.put(key, actionClass);
                ACTION_MAP = newMap;
            }
        }
    }

    public static Action<?> getAction(String application, String taskName) {
        String key = getKey(application, taskName);
        Action<?> action = ACTION_OBJ_MAP.get(key);
        if (action != null) {
            return new ActionAdapter<Object>((Action<Object>) action) {
                @Override
                public Object getParam() {
                    throw new NullPointerException("new action not setting exec param.");
                }
            };
        }
        try {
            Class<? extends Action<?>> actionClass = ACTION_MAP.get(key);
            return actionClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("new action fail. " + e.getMessage());
        }
    }

    public static <T extends Action<?>> void bindAction(String application, String taskName, T actionObj) {
        String key = getKey(application, taskName);
        synchronized (LOCK) {
            if (ACTION_OBJ_MAP.containsKey(key)) {
                ACTION_OBJ_MAP.replace(key, actionObj);
            } else {
                Map<String, Action<?>> newMap = new HashMap<>(ACTION_OBJ_MAP);
                newMap.put(key, actionObj);
                ACTION_OBJ_MAP = newMap;
            }
        }
    }

}
