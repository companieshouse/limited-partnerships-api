package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.*;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/")
public class PscController {
    @PostMapping("/persons-with-significant-control")
    public ResponseEntity<PscSubmissionCreatedResponseDto> createPsc(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                     @RequestBody PscDto pscDto,
                                                                     @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
                                                                     @RequestHeader(value = ERIC_IDENTITY) String userId) {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Create a Psc", logMap);
        var pscId = "123456";
        var location = URI.create(String.format(URL_GET_PSC, transactionId, pscId));
        var response = new PscSubmissionCreatedResponseDto(pscId);
        return ResponseEntity.created(location).body(response);
    }
}
