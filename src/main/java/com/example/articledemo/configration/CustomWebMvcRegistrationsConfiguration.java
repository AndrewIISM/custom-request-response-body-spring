package com.example.articledemo.configration;

import com.example.articledemo.core.method.CustomRequestResponseBodyMethodProcessor;
import com.example.articledemo.core.mvc.method.annotation.CustomRequestMappingHandlerAdapter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
public class CustomWebMvcRegistrationsConfiguration implements WebMvcRegistrations {
    private final CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor;

    public CustomWebMvcRegistrationsConfiguration(CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor) {
        this.customRequestResponseBodyMethodProcessor = customRequestResponseBodyMethodProcessor;
    }

    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new CustomRequestMappingHandlerAdapter(customRequestResponseBodyMethodProcessor);
    }

}
