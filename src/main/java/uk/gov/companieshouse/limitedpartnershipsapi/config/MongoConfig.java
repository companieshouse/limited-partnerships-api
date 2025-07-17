package uk.gov.companieshouse.limitedpartnershipsapi.config;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "uk.gov.companieshouse.limitedpartnershipsapi.repository")
@EnableMongoAuditing( dateTimeProviderRef = "mongodbDatetimeProvider" )
public class MongoConfig {

    @Bean(name = "mongodbDatetimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return() -> Optional.of(LocalDateTime.now());
    }
}
