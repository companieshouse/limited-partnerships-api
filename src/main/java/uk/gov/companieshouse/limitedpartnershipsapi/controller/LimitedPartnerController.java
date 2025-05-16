package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.google.gson.GsonBuilder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_LIMITED_PARTNER_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership")
public class LimitedPartnerController {

    private final LimitedPartnerService limitedPartnerService;
    private final CostsService costsService;

    @Autowired
    public LimitedPartnerController(LimitedPartnerService limitedPartnerService, CostsService costsService) {
        this.limitedPartnerService = limitedPartnerService;
        this.costsService = costsService;
    }

    @PostMapping("/limited-partner")
    public ResponseEntity<LimitedPartnerSubmissionCreatedResponseDto> createLimitedPartner(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody LimitedPartnerDto limitedPartnerDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId)
            throws MethodArgumentNotValidException {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Create a Limited Partner submission", logMap);
        try {
            String submissionId = limitedPartnerService.createLimitedPartner(transaction, limitedPartnerDto, requestId, userId);
            var location = URI.create(String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId));
            var response = new LimitedPartnerSubmissionCreatedResponseDto(submissionId);
            return ResponseEntity.created(location).body(response);
        } catch (ServiceException | NoSuchMethodException e) {
            ApiLogger.errorContext(requestId, "Error creating Limited Partner", e, logMap);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/limited-partner/{" + URL_PARAM_LIMITED_PARTNER_ID + "}")
    public ResponseEntity<Object> updateLimitedPartner(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                       @PathVariable(URL_PARAM_LIMITED_PARTNER_ID) String limitedPartnerId,
                                                       @Valid @RequestBody LimitedPartnerDataDto limitedPartnerDataDto,
                                                       @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
                                                       @RequestHeader(value = ERIC_IDENTITY) String userId)
            throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        ApiLogger.infoContext(requestId, "Update a limited partner", logMap);

        limitedPartnerService.updateLimitedPartner(transaction, limitedPartnerId, limitedPartnerDataDto, requestId, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/limited-partner/{" + URL_PARAM_LIMITED_PARTNER_ID + "}")
    public ResponseEntity<Object> getLimitedPartner(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_LIMITED_PARTNER_ID) String limitedPartnerId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {
        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_LIMITED_PARTNER_ID, limitedPartnerId);

        ApiLogger.infoContext(requestId, "Retrieving a limited partner", logMap);
        try {
            var dto = limitedPartnerService.getLimitedPartner(transaction, limitedPartnerId);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/limited-partners")
    public ResponseEntity<List<LimitedPartnerDto>> getLimitedPartners(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                      @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {
        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Retrieving list of limited partners", logMap);

        return ResponseEntity.ok().body(limitedPartnerService.getLimitedPartnerList(transaction));
    }

    @DeleteMapping("/limited-partner/{" + URL_PARAM_LIMITED_PARTNER_ID + "}")
    public ResponseEntity<Object> deleteLimitedPartner(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                       @PathVariable(URL_PARAM_LIMITED_PARTNER_ID) String limitedPartnerId,
                                                       @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ServiceException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_LIMITED_PARTNER_ID, limitedPartnerId);

        ApiLogger.infoContext(requestId, "Delete a limited partner", logMap);

        limitedPartnerService.deleteLimitedPartner(transaction, limitedPartnerId, requestId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/limited-partner/{" + URL_PARAM_LIMITED_PARTNER_ID + "}/validation-status")
    public ResponseEntity<ValidationStatusResponse> getValidationStatus(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                        @PathVariable(URL_PARAM_LIMITED_PARTNER_ID) String limitedPartnerId,
                                                                        @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ServiceException {

        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transaction.getId());
        logMap.put(URL_PARAM_LIMITED_PARTNER_ID, limitedPartnerId);

        ApiLogger.infoContext(requestId, "Calling service to validate a Limited Partner", logMap);
        var validationStatus = new ValidationStatusResponse();
        validationStatus.setValid(true);

        var validationErrors = limitedPartnerService.validateLimitedPartner(transaction, limitedPartnerId);

        if (!validationErrors.isEmpty()) {
            ApiLogger.errorContext(requestId, String.format("Validation errors: %s",
                    new GsonBuilder().create().toJson(validationErrors)), null, logMap);
            validationStatus.setValid(false);
            validationStatus.setValidationStatusError(validationErrors.toArray(ValidationStatusError[]::new));
        }

        return ResponseEntity.ok().body(validationStatus);
    }

    @GetMapping("/limited-partner/{" + URL_PARAM_LIMITED_PARTNER_ID + "}/costs")
    public ResponseEntity<List<Cost>> getCosts(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_LIMITED_PARTNER_ID) String limitedPartnerId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());
        ApiLogger.infoContext(requestId, "Calling CostsService to retrieve costs", logMap);

        Cost cost = costsService.getTemporaryZeroCost(limitedPartnerId, "Limited Partner", requestId);

        return ResponseEntity.ok(Collections.singletonList(cost));
    }
}
