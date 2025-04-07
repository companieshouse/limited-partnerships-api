package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.service.FilingsService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_FILING_RESOURCE_ID;

@RestController
@RequestMapping("/private/transactions/{transaction_id}/incorporation/limited-partnership/{filing_resource_id}/filings")
public class FilingsController {

    @Autowired
    private FilingsService filingsService;

    @GetMapping
    public ResponseEntity<FilingApi[]> getFilings(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_FILING_RESOURCE_ID) String incorporationId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            HttpServletRequest request) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());
        ApiLogger.infoContext(requestId, "Calling service to retrieve filing", logMap);
        ApiLogger.infoContext(requestId, "Transaction id is " + transaction.getId(), logMap);
       try {
          FilingApi filing = filingsService.generateLimitedPartnerFilings(transaction);
          return ResponseEntity.ok(new FilingApi[] { filing });
       } catch (ServiceException e) {
           ApiLogger.errorContext(requestId, e.getMessage(), e, logMap);
           return ResponseEntity.notFound().build();
       }
    }
}
