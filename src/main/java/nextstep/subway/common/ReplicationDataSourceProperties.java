package nextstep.subway.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("spring.datasource.hikari")
public class ReplicationDataSourceProperties {

    private DataSourceProperty master;
    private List<DataSourceProperty> slaves;

    public DataSourceProperty getMaster() {
        return master;
    }

    public List<DataSourceProperty> getSlaves() {
        return slaves;
    }

    public static class DataSourceProperty {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

    }

}
