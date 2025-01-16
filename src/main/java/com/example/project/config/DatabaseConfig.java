package com.example.project.config;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import jakarta.persistence.EntityManager;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.example.project.repository",
        "com.example.project.repository.readonly",
        "com.example.project.repository.writeonly"
    },
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
public class DatabaseConfig {

    private final Environment env;

    public DatabaseConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Database connection properties
        config.setJdbcUrl(env.getProperty("spring.datasource.url"));
        config.setUsername(env.getProperty("spring.datasource.username"));
        config.setPassword(env.getProperty("spring.datasource.password"));
        
        // Connection pool properties
        config.setMaximumPoolSize(env.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class, 10));
        config.setMinimumIdle(env.getProperty("spring.datasource.hikari.minimum-idle", Integer.class, 5));
        config.setIdleTimeout(env.getProperty("spring.datasource.hikari.idle-timeout", Long.class, 300000L));
        config.setConnectionTimeout(env.getProperty("spring.datasource.hikari.connection-timeout", Long.class, 20000L));
        config.setMaxLifetime(env.getProperty("spring.datasource.hikari.max-lifetime", Long.class, 1200000L));

        // Statement caching properties
        config.addDataSourceProperty("cachePrepStmts", 
            env.getProperty("spring.datasource.hikari.data-source-properties.cachePrepStmts", "true"));
        config.addDataSourceProperty("prepStmtCacheSize", 
            env.getProperty("spring.datasource.hikari.data-source-properties.prepStmtCacheSize", "250"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 
            env.getProperty("spring.datasource.hikari.data-source-properties.prepStmtCacheSqlLimit", "2048"));
        config.addDataSourceProperty("useServerPrepStmts", 
            env.getProperty("spring.datasource.hikari.data-source-properties.useServerPrepStmts", "true"));

        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(
            "com.example.project.entity"
        );

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); //prevents Hibernate from auto-generating schemas
        vendorAdapter.setShowSql(env.getProperty("spring.jpa.properties.hibernate.show_sql", Boolean.class, true));
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        
        // JPA/Hibernate properties
        properties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.show_sql", env.getProperty("spring.jpa.properties.hibernate.show_sql"));
        properties.put("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));
        
        // Performance properties
        properties.put("hibernate.jdbc.batch_size", 
            env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size", "25"));
        properties.put("hibernate.jdbc.fetch_size", 
            env.getProperty("spring.jpa.properties.hibernate.jdbc.fetch_size", "100"));
        properties.put("hibernate.default_batch_fetch_size", 
            env.getProperty("spring.jpa.properties.hibernate.default_batch_fetch_size", "100"));
        properties.put("hibernate.order_updates", 
            env.getProperty("spring.jpa.properties.hibernate.order_updates", "true"));
        properties.put("hibernate.query.in_clause_parameter_padding", 
            env.getProperty("spring.jpa.properties.hibernate.query.in_clause_parameter_padding", "true"));
        
        // Cache properties
        properties.put("hibernate.cache.use_second_level_cache", 
            env.getProperty("spring.jpa.properties.hibernate.cache.use_second_level_cache", "false"));
        properties.put("hibernate.cache.use_query_cache", 
            env.getProperty("spring.jpa.properties.hibernate.cache.use_query_cache", "false"));

        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionMaobnager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Bean
    @Primary
    public EntityManager entityManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return entityManagerFactory.getObject().createEntityManager();
    }

}