package uk.gov.companieshouse.limitedpartnershipsapi;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class LimitedPartnershipsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LimitedPartnershipsApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // This is to prevent times being out of time by an hour during British Summer Time in MongoDB
        // MongoDB stores UTC datetime, and LocalDate doesn't contain timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
