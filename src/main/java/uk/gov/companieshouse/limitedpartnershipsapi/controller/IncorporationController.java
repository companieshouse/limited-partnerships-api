package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.google.gson.GsonBuilder;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_INCORPORATION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/incorporation/limited-partnership")
public class IncorporationController {

    private final LimitedPartnershipIncorporationService incorporationService;
    private final CostsService costsService;

    @Autowired
    public IncorporationController(LimitedPartnershipIncorporationService incorporationService, CostsService costsService) {
        this.incorporationService = incorporationService;
        this.costsService = costsService;
    }

    @PostMapping
    public ResponseEntity<Object> createIncorporation(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody IncorporationDto incorporationDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Calling service to create a Limited Partnership Incorporation", logMap);

        try {
            String submissionId = incorporationService.createIncorporation(transaction, incorporationDto, requestId,
                    userId);
            var location = URI.create(String.format(URL_GET_INCORPORATION, transactionId, submissionId));
            var response = new LimitedPartnershipCreatedResponseDto(submissionId);
            return ResponseEntity.created(location).body(response);
        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, "Error creating Limited Partnership incorporation", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{" + URL_PARAM_INCORPORATION_ID + "}")
    public ResponseEntity<Object> getIncorporation(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_INCORPORATION_ID) String incorporationId,
            @RequestParam(value = "include_sub_resources", required = false) Boolean includeSubResources,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId
    ) {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Calling service to get a Limited Partnership Incorporation, include_sub_resources = " + includeSubResources, logMap);

        try {
            LimitedPartnershipIncorporationDto dto = incorporationService.getIncorporation(transaction, incorporationId, includeSubResources);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        } catch (ServiceException se) {
            ApiLogger.errorContext(requestId, "Error getting a Limited Partnership Incorporation", se, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{" + URL_PARAM_INCORPORATION_ID + "}/validation-status")
    public ResponseEntity<ValidationStatusResponse> getValidationStatus(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_INCORPORATION_ID) String incorporationId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws ServiceException {
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transaction.getId());
        logMap.put(URL_PARAM_INCORPORATION_ID, incorporationId);

        ApiLogger.infoContext(requestId, "Calling service to validate a Limited Partnership Incorporation", logMap);
        var validationStatus = new ValidationStatusResponse();
        validationStatus.setValid(true);

        var validationErrors = incorporationService.validateIncorporation(transaction);

        if (!validationErrors.isEmpty()) {
            ApiLogger.errorContext(requestId, String.format("Validation errors: %s",
                    new GsonBuilder().create().toJson(validationErrors)), null, logMap);
            validationStatus.setValid(false);
            validationStatus.setValidationStatusError(validationErrors.toArray(ValidationStatusError[]::new));
        }

        return ResponseEntity.ok().body(validationStatus);
    }

    @GetMapping("/{" + URL_PARAM_INCORPORATION_ID + "}/costs")
    public ResponseEntity<List<Cost>> getCosts(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_INCORPORATION_ID) String incorporationId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws ResourceNotFoundException {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());
        ApiLogger.infoContext(requestId, "Calling CostsService to retrieve costs", logMap);

        Cost cost = costsService.getCost(incorporationId, requestId);

        return ResponseEntity.ok(Collections.singletonList(cost));
    }
}
