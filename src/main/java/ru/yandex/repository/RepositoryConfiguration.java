package ru.yandex.repository;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Настройка репозиториев
 */
@Configuration
public class RepositoryConfiguration {
    /**
     * Настройка DataSource — компонент, отвечающий за соединение с базой данных
     */
    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * JdbcTemplate — компонент для выполнения запросов
     */
    @Bean
    public NamedParameterJdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Репозиторий управления постами
     */
    @Bean
    public PostRepository postRepository(NamedParameterJdbcTemplate jdbcTemplate, Logger logger) {
        return new JdbcNativePostRepository(jdbcTemplate, logger);
    }
}
