package com.example.articledemo.core.mvc.method.annotation;

import com.example.articledemo.core.annotation.CustomArg;
import com.example.articledemo.domain.EntityWithSmthInfo;
import com.example.articledemo.core.method.CustomRequestResponseBodyMethodProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

public class CustomServletInvocableHandlerMethod extends ServletInvocableHandlerMethod {

    private static final Logger log = LoggerFactory.getLogger(CustomServletInvocableHandlerMethod.class);

    private static final Object[] EMPTY_ARGS = new Object[0];

    private WebDataBinderFactory dataBinderFactory;
    private ParameterNameDiscoverer parameterNameDiscoverer;
    private HandlerMethodArgumentResolverComposite resolvers;


    private final CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor;

    public CustomServletInvocableHandlerMethod(HandlerMethod handlerMethod,
                                               CustomRequestResponseBodyMethodProcessor customRequestResponseBodyMethodProcessor) {

        super(handlerMethod);
        this.customRequestResponseBodyMethodProcessor = customRequestResponseBodyMethodProcessor;
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        this.resolvers = new HandlerMethodArgumentResolverComposite();
    }

    @Override
    protected Object[] getMethodArgumentValues(NativeWebRequest request, ModelAndViewContainer mavContainer,
                                               Object... providedArgs) throws Exception {
        final int argInfoIndexNotExistValue = -1;

        MethodParameter[] parameters = getMethodParameters();
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        EntityWithSmthInfo entityWithSmth = null;
        int infoArgIndex = argInfoIndexNotExistValue;

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = findProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }

            // +++ Check parameter that it is annotated @CustomRequestBody
            if (customRequestResponseBodyMethodProcessor.supportsParameter(parameter)) {
                entityWithSmth = (EntityWithSmthInfo) customRequestResponseBodyMethodProcessor.resolveArgument(parameter,
                        mavContainer, request, this.dataBinderFactory);

                args[i] = entityWithSmth.getEntity();
                continue;
            }

            // +++ Remember position arg with @CustomArg
            if (parameter.hasParameterAnnotation(CustomArg.class)) {
                infoArgIndex = i;
                continue;
            }

            tryUseArgumentResolvers(request, mavContainer, args, i, parameter);
        }


        // +++ Set value for value with @CustomArg
        if (entityWithSmth != null) {
            if (infoArgIndex == argInfoIndexNotExistValue) {
                throw new IllegalStateException("Not found info parameter");
            }

            args[infoArgIndex] = entityWithSmth.getInfo();
        }

        return args;
    }

    private void tryUseArgumentResolvers(NativeWebRequest request, ModelAndViewContainer mavContainer,
                                         Object[] args, int argIndex, MethodParameter parameter) throws Exception {
        if (!this.resolvers.supportsParameter(parameter)) {
            throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
        }

        try {
            args[argIndex] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
        }

        catch (Exception ex) {
            if (log.isDebugEnabled()) {
                String exMsg = ex.getMessage();
                if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
                    log.debug(formatArgumentError(parameter, exMsg));
                }
            }
            throw ex;
        }
    }

    @Override
    public void setDataBinderFactory(@Nullable WebDataBinderFactory dataBinderFactory) {
        this.dataBinderFactory = dataBinderFactory;
    }

    @Override
    public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.resolvers = argumentResolvers;
    }

    @Override
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }
}
