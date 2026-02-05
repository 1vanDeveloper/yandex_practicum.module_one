package ru.yandex.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class LoggingConfiguration {

    @Bean
    @Scope("prototype")
    public Logger logger(InjectionPoint ip) {
        // Определяем класс, в который внедряется логгер
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
    }
}
