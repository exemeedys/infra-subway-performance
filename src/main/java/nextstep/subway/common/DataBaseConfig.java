package nextstep.subway.common;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static nextstep.subway.common.ReplicationRoutingDataSource.DATASOURCE_KEY_MASTER;
import static nextstep.subway.common.ReplicationRoutingDataSource.DATASOURCE_KEY_SLAVE;

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"nextstep.subway"})
public class DataBaseConfig {

//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.hikari.master")
//    public DataSource masterDataSource() {
//        return DataSourceBuilder.create().type(HikariDataSource.class).build();
//    }
//
//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.hikari.slave")
//    public DataSource slaveDataSource() {
//        return DataSourceBuilder.create().type(HikariDataSource.class).build();
//    }

    @Bean
    public DataSource routingDataSource(ReplicationDataSourceProperties replicationDataSourceProperties) {

        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource(replicationDataSourceProperties.getSlaves().size());
        Map<Object, Object> sources = createDataSources(replicationDataSourceProperties);
        routingDataSource.setTargetDataSources(sources);
        routingDataSource.setDefaultTargetDataSource(sources.get(DATASOURCE_KEY_MASTER));
        return routingDataSource;
    }

    private Map<Object, Object> createDataSources(ReplicationDataSourceProperties replicationDataSourceProperties) {
        Map<Object, Object> dataSources = new LinkedHashMap<>();
        dataSources.put(DATASOURCE_KEY_MASTER, createDataSource(replicationDataSourceProperties.getMaster()));

        IntStream.range(0, replicationDataSourceProperties.getSlaves().size()).forEach(i -> {
            dataSources.put(String.format("%s-%d", DATASOURCE_KEY_MASTER, i), createDataSource(replicationDataSourceProperties.getSlaves().get(i)));
        });

        return dataSources;
    }

    private DataSource createDataSource(ReplicationDataSourceProperties.DataSourceProperty dataSourceProperty) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceProperty.getUrl());
        dataSource.setDriverClassName(dataSourceProperty.getDriverClassName());
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        return dataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
