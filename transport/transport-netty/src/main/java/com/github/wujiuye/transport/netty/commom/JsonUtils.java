package com.github.wujiuye.transport.netty.commom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.io.IOException;
import java.util.List;

/**
 * @author wujiuye 2020/12/17
 */
public class JsonUtils {

    private final static ObjectMapper FROM_JSON_JACKSON;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        FROM_JSON_JACKSON = objectMapper;
    }

    public static String toJsonString(Object obj) {
        try {
            if (obj instanceof String) {
                return (String) obj;
            }
            return FROM_JSON_JACKSON.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String jsonStr, Class<T> tClass) {
        try {
            if (tClass == String.class) {
                return (T) jsonStr;
            }
            return FROM_JSON_JACKSON.readValue(jsonStr, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> fromJsonList(String jsonStr, Class<T> eClass) {
        try {
            if (jsonStr == null) {
                return null;
            }
            return FROM_JSON_JACKSON.readValue(jsonStr,
                    CollectionType.construct(List.class,
                            SimpleType.constructUnsafe(eClass)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
