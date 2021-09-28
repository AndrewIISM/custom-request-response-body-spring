package com.example.articledemo.core.method;

import com.example.articledemo.core.annotation.CustomRequestBody;
import com.example.articledemo.core.annotation.CustomResponseBody;
import com.example.articledemo.core.converter.CustomMessageConverter;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.provider.HibernateUtils;
import org.springframework.data.util.ProxyUtils;
import org.springframework.http.*;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CustomRequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor {

    private final CustomMessageConverter converter;

    public CustomRequestResponseBodyMethodProcessor(CustomMessageConverter converter) {
        super(List.of(converter));
        this.converter = converter;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(CustomResponseBody.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws IOException {

        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        writeWithMessageConverter(returnValue, returnType, inputMessage, outputMessage);
    }

    protected void writeWithMessageConverter(Object value, MethodParameter returnType,
                                             ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException {

        Type targetType = GenericTypeResolver.resolveType(getGenericType(returnType), returnType.getContainingClass());

        if (isResourceType(value, returnType)) {
            outputMessage.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");
            if (value != null && inputMessage.getHeaders().getFirst(HttpHeaders.RANGE) != null &&
                    outputMessage.getServletResponse().getStatus() == 200) {

                Resource resource = (Resource) value;

                try {
                    outputMessage.getServletResponse().setStatus(HttpStatus.PARTIAL_CONTENT.value());
                } catch (IllegalArgumentException ex) {
                    outputMessage.getHeaders().set(HttpHeaders.CONTENT_RANGE, "bytes */" + resource.contentLength());
                    outputMessage.getServletResponse().setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                }
            }
        }

        MediaType selectedMediaType = MediaType.APPLICATION_JSON;

        selectedMediaType = selectedMediaType.removeQualityValue();

        Assert.notNull(value, "Written value can't be null");
        converter.write(value, targetType, selectedMediaType, outputMessage);
    }

    private Type getGenericType(MethodParameter returnType) {
        if (HttpEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return ResolvableType.forType(returnType.getGenericParameterType()).getGeneric().getType();
        } else {
            return returnType.getGenericParameterType();
        }
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CustomRequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        Class<?> targetClass = getTargetType(parameter);
        HttpInputMessage inputMessage = new ServletServerHttpRequest(request);

        return converter.readWithSmthInfo(targetClass, inputMessage);
    }

    private Class<?> getTargetType(MethodParameter parameter) {
        Type targetType = parameter.getNestedGenericParameterType();
        Class<?> targetClass = (targetType instanceof Class ? (Class<?>) targetType : null);
        if (targetClass == null) {
            ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
            targetClass = resolvableType.resolve();
        }

        return targetClass;
    }
}
