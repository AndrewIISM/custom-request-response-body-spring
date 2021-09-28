package com.example.articledemo.configration;

import com.example.articledemo.core.method.CustomRequestResponseBodyMethodProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class CustomHandlerMethodConfiguration {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    private final CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor;

    public CustomHandlerMethodConfiguration(RequestMappingHandlerAdapter requestMappingHandlerAdapter,
                                            CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor) {

        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
        this.customRequestResponseBodyMethodProcessor = customRequestResponseBodyMethodProcessor;
    }

    @PostConstruct
    public void init() {
        setReturnValueHttpHandler();
    }

    private void setReturnValueHttpHandler() {
        List<HandlerMethodReturnValueHandler> updatedValues = new ArrayList<>();

        updatedValues.add(0, customRequestResponseBodyMethodProcessor);
        updatedValues.addAll(requestMappingHandlerAdapter.getReturnValueHandlers());

        requestMappingHandlerAdapter.setReturnValueHandlers(updatedValues);
    }

}
