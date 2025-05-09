package com.github.madz0.springbinder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.form.FormObjectBindingArgumentResolver;
import com.github.madz0.springbinder.binding.rest.RestObjectBindingArgumentResolver;
import com.github.madz0.springbinder.binding.rest.serialize.ContextAwareObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

@Configuration
@EnableAutoConfiguration
@RequiredArgsConstructor
public class Config implements WebMvcConfigurer {

    final Map<String, EntityManager> entityManagerMap;
    final ApplicationContext context;
    final @Nullable IdClassMapper idClassMapper;

    private AbstractModelBindingArgumentResolver formObjectModelBindingArgumentResolver;
    private AbstractModelBindingArgumentResolver restObjectModelBindingArgumentResolver;

    @PostConstruct
    public void postConstruct() {
        formObjectModelBindingArgumentResolver = new FormObjectBindingArgumentResolver(entityManagerMap, idClassMapper);
        restObjectModelBindingArgumentResolver = new RestObjectBindingArgumentResolver(entityManagerMap, objectMapper());
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ContextAwareObjectMapper(context);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(formObjectModelBindingArgumentResolver);
        resolvers.add(restObjectModelBindingArgumentResolver);
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(formObjectModelBindingArgumentResolver);
        handlers.add(restObjectModelBindingArgumentResolver);
    }
}
