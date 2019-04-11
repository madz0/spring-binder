package com.github.madz0.springbinder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.binding.AbstractModelBindingArgumentResolver;
import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.form.FormObjectBindingArgumentResolver;
import com.github.madz0.springbinder.binding.rest.RestObjectBindingArgumentResolver;
import com.github.madz0.springbinder.binding.rest.serialize.ContextAwareObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;

@Configuration
@EnableAutoConfiguration
public class Config implements WebMvcConfigurer {
    @Autowired
    EntityManager entityManager;
    @Autowired
    ApplicationContext context;
    @Autowired(required = false)
    IdClassMapper idClassMapper;
    private AbstractModelBindingArgumentResolver formObjectModelBindingArgumentResolver;
    private AbstractModelBindingArgumentResolver restobjectModelBindingArgumentResolver;

    @PostConstruct
    public void postConstruct() {
        formObjectModelBindingArgumentResolver = new FormObjectBindingArgumentResolver(entityManager, idClassMapper);
        restobjectModelBindingArgumentResolver = new RestObjectBindingArgumentResolver(getObjectmapper(), entityManager);
    }

    @Bean
    ObjectMapper getObjectmapper() {
        return new ContextAwareObjectMapper(context);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(formObjectModelBindingArgumentResolver);
        resolvers.add(restobjectModelBindingArgumentResolver);
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(formObjectModelBindingArgumentResolver);
        handlers.add(restobjectModelBindingArgumentResolver);
    }
}
