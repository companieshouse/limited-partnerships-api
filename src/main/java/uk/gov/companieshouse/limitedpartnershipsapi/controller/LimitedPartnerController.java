package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_SUBMISSION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/limited-partner")
public class LimitedPartnerController {

    private final LimitedPartnerService limitedPartnerService;

    @Autowired
    public LimitedPartnerController(LimitedPartnerService limitedPartnerService) {
        this.limitedPartnerService = limitedPartnerService;
    }

    @PostMapping
    public ResponseEntity<LimitedPartnerSubmissionCreatedResponseDto> createLimitedPartner(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody LimitedPartnerDto limitedPartnerDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            ApiLogger.infoContext(requestId, "Create a Limited Partner submission", logMap);

            var submissionId = limitedPartnerService.createLimitedPartner(transaction, limitedPartnerDto, requestId,
                    userId);

            var location = URI.create(String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId));

            var response = new LimitedPartnerSubmissionCreatedResponseDto(submissionId);
            return ResponseEntity.created(location).body(response);

        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, "Error creating Limited Partner", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{" + URL_PARAM_SUBMISSION_ID + "}")
    public ResponseEntity<Object> getLimitedPartner(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_SUBMISSION_ID, submissionId);

        try {
            ApiLogger.infoContext(requestId, "Retrieving a limited partner", logMap);
            LimitedPartnerDto dto = limitedPartnerService.getLimitedPartner(transaction, submissionId);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }
}
