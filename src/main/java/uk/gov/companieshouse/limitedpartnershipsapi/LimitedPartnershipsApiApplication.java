package uk.gov.companieshouse.limitedpartnershipsapi;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.TimeZone;

@SpringBootApplication
public class LimitedPartnershipsApiApplication implements CommandLineRunner {

    @Autowired
    private LimitedPartnershipService service;

    public static final String APP_NAMESPACE = "limited-partnerships-api";
    public static void main(String[] args) {
        ApiLogger.debug("Starting Limited Partnerships API");
        SpringApplication.run(LimitedPartnershipsApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // This is to prevent times being out of time by an hour during British Summer Time in MongoDB
        // MongoDB stores UTC datetime, and LocalDate doesn't contain timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void run(String... args) throws Exception {
        DataDto data = new DataDto();
        data.setPartnershipName("Joe Bloggs");
        data.setNameEnding("Limited Partnership");

        LimitedPartnershipSubmissionDto dto = new LimitedPartnershipSubmissionDto();
        dto.setData(data);
        ApiLogger.debug("Persisting LP");
        String id = service.createLimitedPartnership(dto, "12345");
        ApiLogger.debug("Persisted LP: " + id);
    }
}
