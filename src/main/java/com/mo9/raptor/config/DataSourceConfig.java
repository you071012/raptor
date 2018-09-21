package com.mo9.raptor.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wtwei .
 * @date 2018/9/14 .
 * @time 17:47 .
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.primary.driver-class-name}")
    private String primaryDriverClassName;

    @Value("${spring.datasource.primary.username}")
    private String primaryUsername;

    @Value("${spring.datasource.primary.password}")
    private String primaryPassword;

    @Value("${spring.datasource.primary.jdbc-url}")
    private String primaryUrl;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String riskDriverClassName;

    @Value("${spring.datasource.secondary.username}")
    private String riskUsername;

    @Value("${spring.datasource.secondary.password}")
    private String riskPassword;

    @Value("${spring.datasource.secondary.jdbc-url}")
    private String riskUrl;

    @Primary
    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
//    @ConfigurationProperties(prefix="spring.datasource.primary")
    public DataSource primaryDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(primaryDriverClassName);
        dataSource.setUsername(primaryUsername);
        dataSource.setPassword(primaryPassword);
        dataSource.setUrl(primaryUrl);
        List<String> connectionInitSqls = new ArrayList<>();
        connectionInitSqls.add("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
        dataSource.setConnectionInitSqls(connectionInitSqls);
        return dataSource;
    }

    @Bean(name = "secondaryDataSource")
    @Qualifier("secondaryDataSource")
//    @ConfigurationProperties(prefix="spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(riskDriverClassName);
        dataSource.setUsername(riskUsername);
        dataSource.setPassword(riskPassword);
        dataSource.setUrl(riskUrl);
        return dataSource;
    }

}
