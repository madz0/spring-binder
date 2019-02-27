package com.github.madz0.springbinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.iiscenter.springform.SpringFormObjectMapper;
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
        return new SpringFormObjectMapper(context);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new FormEntityModelBindingArgumentResolver(entityManager));
    }
}
