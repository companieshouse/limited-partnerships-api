package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.PersonWithSignificantControlService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_IDENTITY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_PARAM_TRANSACTION_ID;

@RestController
@RequestMapping("/transactions/{" + URL_PARAM_TRANSACTION_ID + "}/limited-partnership/")
public class PersonWithSignificantControlController {

    private final PersonWithSignificantControlService personWithSignificantControlService;

    @Autowired
    public PersonWithSignificantControlController(PersonWithSignificantControlService personWithSignificantControlService) {
        this.personWithSignificantControlService = personWithSignificantControlService;
    }

    @GetMapping("/person-with-significant-control/{" + URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID + "}")
    public ResponseEntity<PersonWithSignificantControlDto> getPersonWithSignificantControl(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                                           @PathVariable(URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID) String personWithSignificantControlId,
                                                                                           @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID, personWithSignificantControlId);

        ApiLogger.infoContext(requestId, String.format("Retrieving a person with significant control %s", personWithSignificantControlId), logMap);
        var dto = personWithSignificantControlService.getPersonWithSignificantControl(transaction, personWithSignificantControlId);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/person-with-significant-control")
    public ResponseEntity<PersonWithSignificantControlSubmissionCreatedResponseDto> createPersonWithSignificantControl(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @RequestBody PersonWithSignificantControlDto personWithSignificantControlDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        ApiLogger.infoContext(requestId, "Create a person with significant control", logMap);
        try {
            String pscId = personWithSignificantControlService.createPersonWithSignificantControl(transaction, personWithSignificantControlDto, requestId, userId);
            var location = URI.create(String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, transactionId, pscId));
            var response = new PersonWithSignificantControlSubmissionCreatedResponseDto(pscId);
            return ResponseEntity.created(location).body(response);
        } catch (Exception e) {
            ApiLogger.errorContext(requestId, "Error creating the person with significant control", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/person-with-significant-control/{" + URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID + "}")
    public ResponseEntity<Object> updatePersonWithSignificantControl(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID) String personWithSignificantControlId,
            @Valid @RequestBody PersonWithSignificantControlDataDto personWithSignificantControlDataDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) throws ResourceNotFoundException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID, personWithSignificantControlId);

        ApiLogger.infoContext(requestId, String.format("Updating a person with significant control %s", personWithSignificantControlId), logMap);
        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, personWithSignificantControlId, personWithSignificantControlDataDto, requestId, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/person-with-significant-control/{" + URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID + "}")
    public ResponseEntity<Object> deletePersonWithSignificantControl(@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
                                                                     @PathVariable(URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID) String personWithSignificantControlId,
                                                                     @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId)
            throws ServiceException {

        String transactionId = transaction.getId();
        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(URL_PARAM_TRANSACTION_ID, transactionId);
        logMap.put(URL_PARAM_PERSON_WITH_SIGNIFICANT_CONTROL_ID, personWithSignificantControlId);

        ApiLogger.infoContext(requestId, "Delete a person with significant control", logMap);

        personWithSignificantControlService.deletePersonWithSignificantControl(transaction, personWithSignificantControlId, requestId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
