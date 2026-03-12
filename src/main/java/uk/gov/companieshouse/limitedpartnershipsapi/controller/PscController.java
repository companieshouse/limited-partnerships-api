package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.PscService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.*;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/")
public class PscController {

    private final PscService pscService;

    @Autowired
    public PscController(PscService pscService) {
        this.pscService = pscService;
    }

    @PostMapping("/persons-with-significant-control")
    public ResponseEntity<PscSubmissionCreatedResponseDto> createPsc(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                     @RequestBody PscDto pscDto,
                                                                     @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
                                                                     @RequestHeader(value = ERIC_IDENTITY) String userId)
            throws ServiceException, MethodArgumentNotValidException {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Create a Psc", logMap);
        try {
            String pscId = pscService.createPsc(transaction, pscDto, requestId, userId);
            var location = URI.create(String.format(URL_GET_PSC, transactionId, pscId));
            var response = new PscSubmissionCreatedResponseDto(pscId);
            return ResponseEntity.created(location).body(response);
        } catch (NoSuchMethodException e) {
            ApiLogger.errorContext(requestId, "Error creating the person of significant control", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
