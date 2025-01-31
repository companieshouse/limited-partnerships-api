package uk.gov.companieshouse.limitedpartnershipsapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.TimeZone;

@SpringBootApplication
public class LimitedPartnershipsApiApplication {
    public static final String APP_NAMESPACE = "limited-partnerships-api";

    public static void main(String[] args) {
        ApiLogger.debug("Starting Limited Partnerships API");
        SpringApplication.run(LimitedPartnershipsApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // This is to prevent times being out of time by an hour during British Summer Time in MongoDB
        // MongoDB stores UTC datetime, and LocalDate does not contain timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }
}