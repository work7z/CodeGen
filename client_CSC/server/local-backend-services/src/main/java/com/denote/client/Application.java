package com.denote.client;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.denote.client.config.WebMvcConfig;
import com.denote.client.handler.GlobalController;
import com.denote.client.handler.WebListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
//@ComponentScan(basePackages = {"com.denote.client.handler","com.denote.client.config"}, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.denote.client" + ".web.unuse.*"))
//        basePackages = {"com.denote.client.handler","com.denote.client.config"}

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class})
@ComponentScan(
        basePackageClasses = {GlobalController.class, WebListener.class, WebMvcConfig.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.denote.client" + ".web.unuse.*"))
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .beanNameGenerator(new CustomBeanNameGenerator())
                .run(args);
    }

    private static class CustomBeanNameGenerator implements BeanNameGenerator {
        @Override
        public String generateBeanName(BeanDefinition d, BeanDefinitionRegistry r) {
            return d.getBeanClassName();
        }
    }
}

