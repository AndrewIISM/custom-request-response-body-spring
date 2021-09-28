package com.example.articledemo.core.mvc.method.annotation;

import com.example.articledemo.core.method.CustomRequestResponseBodyMethodProcessor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

public class CustomRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
    private final CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor;

    public CustomRequestMappingHandlerAdapter(CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor) {
        this.customRequestResponseBodyMethodProcessor = customRequestResponseBodyMethodProcessor;
    }

    @Override
    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new CustomServletInvocableHandlerMethod(handlerMethod, customRequestResponseBodyMethodProcessor);
    }
}
