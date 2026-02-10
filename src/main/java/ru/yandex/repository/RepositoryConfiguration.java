package ru.yandex.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        // Оптимизации для Postgres
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        return new HikariDataSource(config);
    }

    /**
     * JdbcTemplate — компонент для выполнения запросов
     */
    @Bean
    public NamedParameterJdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Управление транзакциями
     */
    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        var manager = new DataSourceTransactionManager(dataSource);
        return new TransactionTemplate(manager);
    }

    /**
     * Репозиторий управления постами
     */
    @Bean
    public PostRepository postRepository(NamedParameterJdbcTemplate jdbcTemplate, Logger logger) {
        return new JdbcNativePostRepository(jdbcTemplate, logger);
    }

    /**
     * Репозиторий управления изображениями
     */
    @Bean
    public ImageRepository imageRepository(NamedParameterJdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        return new JdbcNativeImageRepository(jdbcTemplate, transactionTemplate);
    }
}
