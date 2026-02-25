package ru.yandex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.concurrent.Executor;

@Configuration
@EnableWebMvc
@EnableAsync
@ComponentScan(basePackages = {"org.springdoc", "ru.yandex"})
@PropertySource("classpath:application.properties")
public class WebConfiguration {

    @Bean(name = "BlogThreadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(24);
        executor.setThreadNamePrefix("BlogPool-");
        executor.initialize();
        return executor;
    }
}