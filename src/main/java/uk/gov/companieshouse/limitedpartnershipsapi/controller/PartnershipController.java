package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_SUBMISSION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/partnership")
public class PartnershipController {

    private static final String VALIDATION_ERRORS_MESSAGE = "Validation errors : %s";

    private final LimitedPartnershipService limitedPartnershipService;

    @Autowired
    public PartnershipController(LimitedPartnershipService limitedPartnershipService) {
        this.limitedPartnershipService = limitedPartnershipService;
    }

    @PostMapping
    public ResponseEntity<Object> createPartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            ApiLogger.infoContext(requestId, "Calling service to create a Limited Partnership Submission", logMap);

            var submissionId = limitedPartnershipService.createLimitedPartnership(transaction, limitedPartnershipSubmissionDto, requestId, userId);

            var location = URI.create(String.format(URL_GET_PARTNERSHIP, transactionId, submissionId));
            var response = new LimitedPartnershipSubmissionCreatedResponseDto(submissionId);

            return ResponseEntity.created(location).body(response);
        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, "Error creating Limited Partnership submission", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{" + URL_PARAM_SUBMISSION_ID + "}")
    public ResponseEntity<Object> updatePartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
            @Valid @RequestBody LimitedPartnershipPatchDto limitedPartnershipPatchDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            limitedPartnershipService.updateLimitedPartnership(transaction, submissionId, limitedPartnershipPatchDto, requestId, userId);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, "Error updating Limited Partnership submission", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{" + URL_PARAM_SUBMISSION_ID + "}")
    public ResponseEntity<Object> getPartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId
    ) {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            LimitedPartnershipSubmissionDto dto = limitedPartnershipService.getLimitedPartnership(transaction, submissionId);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{" + URL_PARAM_SUBMISSION_ID + "}/validation-status")
    public ResponseEntity<Object> getValidationStatus(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                      @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
                                                      @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transaction.getId());

        try {
            ApiLogger.infoContext(requestId, "Calling service to validate a Limited Partnership Submission", logMap);
            var validationStatus = new ValidationStatusResponse();
            validationStatus.setValid(true);

            var validationErrors = limitedPartnershipService.validateLimitedPartnership(transaction, submissionId);

            if (!validationErrors.isEmpty()) {
                flagValidationStatusAsFailed(validationStatus, validationErrors);
                ApiLogger.errorContext(requestId, String.format(VALIDATION_ERRORS_MESSAGE, validationErrors), null, logMap);
            }

            return ResponseEntity.ok().body(validationStatus);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }

    private void flagValidationStatusAsFailed(ValidationStatusResponse validationStatus, List<String> validationErrors) {
        validationStatus.setValid(false);

        // A simplified 'errors' object can be created as LP registrations are not software filed
        List<ValidationStatusError> errors = new ArrayList<ValidationStatusError>();
        validationErrors.stream().forEach(error -> {
            var statusError = new ValidationStatusError();
            statusError.setError(error);
            errors.add(statusError);
        });

        validationStatus.setValidationStatusError((ValidationStatusError[]) errors.toArray(new ValidationStatusError[0]));
    }
}
