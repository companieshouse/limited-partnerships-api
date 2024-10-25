package uk.gov.companieshouse.limitedpartnershipsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

@SpringBootApplication
public class LimitedPartnershipsApiApplication {
    public static final String APP_NAMESPACE = "limited-partnerships-api";
    public static void main(String[] args) {
        ApiLogger.debug("Starting Limited Partnerships API");
        SpringApplication.run(LimitedPartnershipsApiApplication.class, args);
    }
}
