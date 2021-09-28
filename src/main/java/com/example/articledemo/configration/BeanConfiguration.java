package com.example.articledemo.configration;

import com.example.articledemo.core.converter.CustomMessageConverter;
import com.example.articledemo.core.converter.impl.MappingCustomJackson2HttpMessageConverter;
import com.example.articledemo.core.method.CustomRequestResponseBodyMethodProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CustomMessageConverter customMessageConverter() {
        return new MappingCustomJackson2HttpMessageConverter();
    }

    @Bean
    public CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor() {
        return new CustomRequestResponseBodyMethodProcessor(customMessageConverter());
    }

}
