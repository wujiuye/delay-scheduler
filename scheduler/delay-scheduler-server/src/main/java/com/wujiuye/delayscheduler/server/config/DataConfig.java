package com.wujiuye.delayscheduler.server.config;

/**
 * @author wujiuye 2021/01/12
 */
public class DataConfig {

    private String storageRootPath;

    public void setStorageRootPath(String storageRootPath) {
        this.storageRootPath = storageRootPath;
    }

    public String getStorageRootPath() {
        return storageRootPath;
    }

    @Override
    public String toString() {
        return "DataConfig{" +
                "storageRootPath='" + storageRootPath + '\'' +
                '}';
    }

}
