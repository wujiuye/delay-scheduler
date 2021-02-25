package com.wujiuye.delayscheduler.server.common;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Yaml配置文件读取器
 *
 * @param <T>
 * @author wujiuye 2021/01/12
 */
public abstract class YamlReader<T> {

    private String yamlFileWithClasspath;
    private Class<T> tClass;

    protected YamlReader(String yamlFileWithClasspath) {
        this.yamlFileWithClasspath = yamlFileWithClasspath;
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new UnsupportedOperationException("type fail！");
        } else {
            this.tClass = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    public T loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(yamlFileWithClasspath)) {
            return yaml.loadAs(inputStream, tClass);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
