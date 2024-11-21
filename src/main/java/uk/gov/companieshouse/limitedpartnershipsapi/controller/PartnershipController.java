package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/partnership")
public class PartnershipController {

    static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";

    private final LimitedPartnershipService limitedPartnershipService;

    @Autowired
    public PartnershipController(LimitedPartnershipService limitedPartnershipService) {
        this.limitedPartnershipService = limitedPartnershipService;
    }

    @PostMapping
    public ResponseEntity<Object> createPartnership(
            @RequestBody LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        var transactionId = 12321123;
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            ApiLogger.infoContext(requestId, "Calling service to create Partnership Submission", logMap);

            var submissionId = limitedPartnershipService.createLimitedPartnership(limitedPartnershipSubmissionDto, requestId, userId);

            var location = URI.create(String.format(URL_GET_PARTNERSHIP, transactionId, submissionId));
            var response = new LimitedPartnershipSubmissionCreatedResponseDto(submissionId);

            return ResponseEntity.created(location).body(response);
        } catch (Exception e) {
            ApiLogger.errorContext(requestId, "Error Creating Limited Partnership Submission", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
