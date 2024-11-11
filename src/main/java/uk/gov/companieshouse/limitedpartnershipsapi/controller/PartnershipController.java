package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/partnership")
public class PartnershipController {

    @PostMapping
    public ResponseEntity<Object> createPartnership(
            HttpServletRequest request) {

        ApiLogger.debug("createPartnership");

        URI location = URI.create("/transactions/12321123/limited-partnership/partnership/3235233232");
        LimitedPartnershipSubmissionCreatedResponseDto response = new LimitedPartnershipSubmissionCreatedResponseDto();
        response.setId("3235233232");
        return ResponseEntity.created(location).body(response);
    }
}
