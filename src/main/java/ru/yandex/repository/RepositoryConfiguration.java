package ru.yandex.repository;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

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
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Репозиторий управления постами
     */
    @Bean
    public PostRepository postRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcNativePostRepository(jdbcTemplate);
    }
}
