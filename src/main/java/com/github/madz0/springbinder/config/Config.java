package com.github.madz0.springbinder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.binding.form.FormObjectBindingArgumentResolver;
import com.github.madz0.springbinder.binding.rest.RestObjectBindingArgumentResolver;
import com.github.madz0.springbinder.binding.rest.serialize.ContextAwareObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManager;
import java.util.List;

@Configuration
@EnableAutoConfiguration
public class Config implements WebMvcConfigurer {
    @Autowired
    EntityManager entityManager;
    @Autowired
    ApplicationContext context;
    @Bean
    ObjectMapper getObjectmapper() {
        return new ContextAwareObjectMapper(context);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new FormObjectBindingArgumentResolver(entityManager));
        resolvers.add(new RestObjectBindingArgumentResolver(getObjectmapper(), entityManager));
    }
}
