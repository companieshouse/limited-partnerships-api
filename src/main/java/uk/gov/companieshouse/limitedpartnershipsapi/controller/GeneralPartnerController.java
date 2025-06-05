package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.google.gson.GsonBuilder;
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
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_GENERAL_PARTNER_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership")
public class GeneralPartnerController {

    private final GeneralPartnerService generalPartnerService;
    private final CostsService costsService;

    @Autowired
    public GeneralPartnerController(GeneralPartnerService generalPartnerService, CostsService costsService) {
        this.generalPartnerService = generalPartnerService;
        this.costsService = costsService;
    }

    @GetMapping("/general-partner/{" + URL_PARAM_GENERAL_PARTNER_ID + "}")
    public ResponseEntity<GeneralPartnerDto> getGeneralPartner(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                               @PathVariable(URL_PARAM_GENERAL_PARTNER_ID) String generalPartnerId,
                                                               @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_GENERAL_PARTNER_ID, generalPartnerId);

        ApiLogger.infoContext(requestId, String.format("Retrieving a general partner %s", generalPartnerId), logMap);
        var dto = generalPartnerService.getGeneralPartner(transaction, generalPartnerId);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/general-partner")
    public ResponseEntity<GeneralPartnerSubmissionCreatedResponseDto> createGeneralPartner(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                                           @RequestBody GeneralPartnerDto generalPartnerDto,
                                                                                           @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
                                                                                           @RequestHeader(value = ERIC_IDENTITY) String userId)
            throws ServiceException, MethodArgumentNotValidException {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Create a general partner", logMap);
        try {
            String submissionId = generalPartnerService.createGeneralPartner(transaction, generalPartnerDto, requestId, userId);
            var location = URI.create(String.format(URL_GET_GENERAL_PARTNER, transactionId, submissionId));
            var response = new GeneralPartnerSubmissionCreatedResponseDto(submissionId);
            return ResponseEntity.created(location).body(response);
        } catch (NoSuchMethodException e) {
            ApiLogger.errorContext(requestId, "Error creating the general partner", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/general-partner/{" + URL_PARAM_GENERAL_PARTNER_ID + "}")
    public ResponseEntity<Object> updateGeneralPartner(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                       @PathVariable(URL_PARAM_GENERAL_PARTNER_ID) String generalPartnerId,
                                                       @RequestBody GeneralPartnerDataDto generalPartnerDataDto,
                                                       @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
                                                       @RequestHeader(value = ERIC_IDENTITY) String userId)
            throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        ApiLogger.infoContext(requestId, "Update a general partner", logMap);

        generalPartnerService.updateGeneralPartner(transaction, generalPartnerId, generalPartnerDataDto, requestId, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/general-partner/{" + URL_PARAM_GENERAL_PARTNER_ID + "}/validation-status")
    public ResponseEntity<ValidationStatusResponse> getValidationStatus(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                        @PathVariable(URL_PARAM_GENERAL_PARTNER_ID) String generalPartnerId,
                                                                        @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ServiceException {

        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transaction.getId());
        logMap.put(URL_PARAM_GENERAL_PARTNER_ID, generalPartnerId);

        ApiLogger.infoContext(requestId, "Calling service to validate a General Partner", logMap);
        var validationStatus = new ValidationStatusResponse();
        validationStatus.setValid(true);

        var validationErrors = generalPartnerService.validateGeneralPartner(transaction, generalPartnerId);

        if (!validationErrors.isEmpty()) {
            ApiLogger.errorContext(requestId, String.format("Validation errors: %s",
                    new GsonBuilder().create().toJson(validationErrors)), null, logMap);
            validationStatus.setValid(false);
            validationStatus.setValidationStatusError(validationErrors.toArray(ValidationStatusError[]::new));
        }

        return ResponseEntity.ok().body(validationStatus);
    }

    @GetMapping("/general-partners")
    public ResponseEntity<List<GeneralPartnerDto>> getGeneralPartners(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                      @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws ServiceException {
        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Retrieving list of general partners", logMap);

        return ResponseEntity.ok().body(generalPartnerService.getGeneralPartnerList(transaction));
    }

    @DeleteMapping("/general-partner/{" + URL_PARAM_GENERAL_PARTNER_ID + "}")
    public ResponseEntity<Object> deleteGeneralPartner(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                       @PathVariable(URL_PARAM_GENERAL_PARTNER_ID) String generalPartnerId,
                                                       @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ServiceException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_GENERAL_PARTNER_ID, generalPartnerId);

        ApiLogger.infoContext(requestId, "Delete a general partner", logMap);

        generalPartnerService.deleteGeneralPartner(transaction, generalPartnerId, requestId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/general-partner/{" + URL_PARAM_GENERAL_PARTNER_ID + "}/costs")
    public ResponseEntity<List<Cost>> getCosts(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_GENERAL_PARTNER_ID) String generalPartnerId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());
        ApiLogger.infoContext(requestId, "Calling CostsService to retrieve costs", logMap);

        Cost cost = costsService.getTemporaryZeroCost(generalPartnerId, "General Partner", requestId);

        return ResponseEntity.ok(Collections.singletonList(cost));
    }
}
