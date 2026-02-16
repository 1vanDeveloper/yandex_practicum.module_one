package ru.yandex.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Настройка сериализации и десериализации DTO
 */
@Configuration
public class RestConfiguration {

    @Bean
    public HttpMessageConverter<Object> httpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
