package com.example.articledemo.core.converter;

import com.example.articledemo.domain.EntityWithSmthInfo;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.GenericHttpMessageConverter;

public interface CustomMessageConverter extends GenericHttpMessageConverter<Object> {
    EntityWithSmthInfo readWithSmthInfo(Class<?> clazz, HttpInputMessage inputMessage) throws Exception;
}
