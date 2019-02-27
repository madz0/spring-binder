package com.github.madzo.springbinder;

import com.github.madz0.springbinder.FormEntityModelBindingArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootApplication
public class App implements WebMvcConfigurer {

    @Autowired
    EntityManager entityManager;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new FormEntityModelBindingArgumentResolver(entityManager));
    }
}
