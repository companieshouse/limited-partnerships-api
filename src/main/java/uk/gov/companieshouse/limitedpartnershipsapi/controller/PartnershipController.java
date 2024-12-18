package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.DataType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_SUBMISSION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership")
public class PartnershipController {

    static final String URL_GET_PARTNERSHIP = "/transactions/%s/limited-partnership/partnership/%s";

    private final LimitedPartnershipService limitedPartnershipService;

    @Autowired
    public PartnershipController(LimitedPartnershipService limitedPartnershipService) {
        this.limitedPartnershipService = limitedPartnershipService;
    }

    @PostMapping("/partnership")
    public ResponseEntity<Object> createPartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @RequestBody LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
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

    @PatchMapping("/partnership/{" + URL_PARAM_SUBMISSION_ID + "}")
    public ResponseEntity<Object> updatePartnership(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId
    ) {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            String type = (String) body.get("type");
            var dataType = DataType.valueOf(type.toUpperCase());
            final Map<String, Object> data = extractData(body);

            limitedPartnershipService.updateLimitedPartnership(submissionId, dataType, data);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (JsonProcessingException | ServiceException e) {
            ApiLogger.errorContext(requestId, "Error updating Limited Partnership submission", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{" +URL_PARAM_SUBMISSION_ID + "}")
    public ResponseEntity<Object> getPartnership(
        @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
        @PathVariable(URL_PARAM_SUBMISSION_ID) String submissionId,
        @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
        @RequestHeader(value = ERIC_IDENTITY) String userId
    ){
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);

        try {
            LimitedPartnershipSubmissionDto dto = limitedPartnershipService.getLimitedPartnership(submissionId);
            return ResponseEntity.ok().body(dto);
        } catch (ResourceNotFoundException e){
            ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        }
    }

    private static Map<String, Object> extractData(Map<String, Object> body) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        var json = ow.writeValueAsString(body.get("data"));

        return new ObjectMapper().readValue(json, Map.class);
    }


}
