package uk.gov.companieshouse.limitedpartnershipsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LimitedPartnershipsApiApplication {
    public static final String APP_NAMESPACE = "overseas-entities-api";
    public static void main(String[] args) {
        SpringApplication.run(LimitedPartnershipsApiApplication.class, args);
    }
}
