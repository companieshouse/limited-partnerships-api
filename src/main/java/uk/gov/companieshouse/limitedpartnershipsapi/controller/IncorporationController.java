package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_FILING_RESOURCE_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/incorporation/limited-partnership")
public class IncorporationController {

    private final LimitedPartnershipIncorporationService incorporationService;

    @Autowired
    public IncorporationController(LimitedPartnershipIncorporationService incorporationService) {
        this.incorporationService = incorporationService;
    }

    @PostMapping
    public ResponseEntity<Object> createIncorporation(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Calling service to create a Limited Partnership Incorporation", logMap);

        String submissionId = incorporationService.createIncorporation(userId, transactionId);
        var location = URI.create(String.format(URL_GET_INCORPORATION, transactionId, submissionId));
        var response = new LimitedPartnershipSubmissionCreatedResponseDto(submissionId);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{" + URL_PARAM_FILING_RESOURCE_ID + "}")
    public ResponseEntity<Object> getIncorporation(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_FILING_RESOURCE_ID) String filingResourceId,
            @RequestParam(value = "include_sub_resources", required = true) boolean includeSubResources,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId
    ) {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Calling service to get a Limited Partnership Incorporation, include_sub_resources = " + includeSubResources, logMap);

        try {
            LimitedPartnershipIncorporationDto dto = incorporationService.getIncorporation(transaction, filingResourceId, includeSubResources);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }
}
