package org.example.batchv5.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDbConfig {
    @Primary // batch는 primary 설정된 db 에 메타데이터를 넣기 때문에, primary 설정해야한다.
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource-meta") // yml에 저장된 값을 가져옴
    public DataSource metaDBSource() {

        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager metaTransactionManager() { // 트랜잭션 설정

        return new DataSourceTransactionManager(metaDBSource());
    }
}
