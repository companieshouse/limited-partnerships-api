package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/partnership")
public class PartnershipController {

    @PostMapping
    public ResponseEntity<Object> createPartnership(
            @RequestBody LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId,
            HttpServletRequest request) {

        var logMap = new HashMap<String, Object>();

        try {
            ApiLogger.infoContext(requestId, "Calling service to create Partnership Submission", logMap);

            URI location = URI.create("/transactions/12321123/limited-partnership/partnership/3235233232");
            LimitedPartnershipSubmissionCreatedResponseDto response = new LimitedPartnershipSubmissionCreatedResponseDto();
            response.setId("3235233232");
            return ResponseEntity.created(location).body(response);
        } catch (Exception e) {
            ApiLogger.errorContext(requestId, "Error Creating Limited Partnership Submission", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
