package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import com.google.gson.GsonBuilder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.Collections;
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

    private final PartnershipService partnershipService;
    private final CostsService costsService;

    @Autowired
    public PartnershipController(
        PartnershipService partnershipService,
            CostsService costsService) {
        this.partnershipService = partnershipService;
        this.costsService = costsService;
    }

    @PostMapping
    public ResponseEntity<Object> createPartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @RequestBody PartnershipDto partnershipDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId)
            throws MethodArgumentNotValidException, NoSuchMethodException {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            ApiLogger.infoContext(requestId, "Calling service to create a Limited Partnership", logMap);

            var submissionId = partnershipService.createLimitedPartnership(transaction,
                partnershipDto, requestId, userId);

            var location = URI.create(String.format(URL_GET_PARTNERSHIP, transactionId, submissionId));
            var response = new PartnershipCreatedResponseDto(submissionId);

            return ResponseEntity.created(location).body(response);
        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, "Error creating Limited Partnership", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{" + URL_PARAM_SUBMISSION_ID + "}")
    public ResponseEntity<Object> updatePartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
            @Valid @RequestBody PartnershipPatchDto partnershipPatchDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) throws MethodArgumentNotValidException, NoSuchMethodException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_SUBMISSION_ID, submissionId);

        try {
            partnershipService.updateLimitedPartnership(transaction, submissionId, partnershipPatchDto, requestId, userId);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, "Error updating Limited Partnership", e, logMap);
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
            PartnershipDto dto = partnershipService.getLimitedPartnership(transaction, submissionId);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{" + URL_PARAM_SUBMISSION_ID + "}/validation-status")
    public ResponseEntity<ValidationStatusResponse> getValidationStatus(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws ServiceException {
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transaction.getId());
        logMap.put(URL_PARAM_SUBMISSION_ID, submissionId);

        try {
            ApiLogger.infoContext(requestId, "Calling service to validate a Limited Partnership Submission", logMap);
            var validationStatus = new ValidationStatusResponse();
            validationStatus.setValid(true);

            var validationErrors = partnershipService.validateLimitedPartnership(transaction);

            if (!validationErrors.isEmpty()) {
                ApiLogger.errorContext(requestId, String.format("Validation errors: %s",
                        new GsonBuilder().create().toJson(validationErrors)), null, logMap);
                validationStatus.setValid(false);
                validationStatus.setValidationStatusError(validationErrors.toArray(ValidationStatusError[]::new));
            }

            return ResponseEntity.ok().body(validationStatus);
        } catch (ResourceNotFoundException | MethodArgumentNotValidException | NoSuchMethodException e) {
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{" + URL_PARAM_SUBMISSION_ID + "}/costs")
    public ResponseEntity<List<Cost>> getCosts(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String limitedPartnershipId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws ServiceException {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());
        ApiLogger.infoContext(requestId, String.format("Calling CostsService to retrieve costs for limitedPartnership: %s", limitedPartnershipId), logMap);

        Cost cost = costsService.getPostTransitionLimitedPartnershipCost(transaction);

        return ResponseEntity.ok(Collections.singletonList(cost));
    }
}
