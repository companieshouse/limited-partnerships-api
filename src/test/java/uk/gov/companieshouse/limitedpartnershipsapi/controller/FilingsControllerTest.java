package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.service.FilingsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilingsControllerTest {

    private static String INCORPORATION_ID = "sub123";
    private static String REQUEST_ID = "req123";

    @InjectMocks
    FilingsController filingsController;

    @Mock
    FilingsService filingsService;

    @Test
    void testWhenFilingIsGenerated() throws ServiceException {
        var filingApi = new FilingApi();
        when(filingsService.generateLimitedPartnerFilings(any())).thenReturn(filingApi);
        var response = filingsController.getFilings(new Transaction(), INCORPORATION_ID, REQUEST_ID, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testWhenErrorIsReturned(){

    }

}
