package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.PscService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder.TRANSACTION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PSC;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PSC;

@ExtendWith(MockitoExtension.class)
class PscControllerTest {
    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String PSC_ID = PscBuilder.ID;

    @InjectMocks
    private PscController pscController;

    @Mock
    private PscService pscService;

    private final Transaction transaction = new TransactionBuilder()
        .withKindAndUri(
            FILING_KIND_PSC,
            URL_GET_PSC,
            PSC_ID
        )
        .build();

    private PscDto pscDto;

    @BeforeEach
    void init() { pscDto = PscBuilder.getPscDto(); }

    @Test
    void testGetPscReturnsDto() throws ServiceException {
        PscDto dto = new PscDto();
        when(pscService.getPsc(transaction, PSC_ID))
                .thenReturn(dto);

        var response = pscController.getPsc(transaction, PSC_ID, REQUEST_ID);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPscThrowsResourceNotFoundException() throws ServiceException {
        when(pscService.getPsc(any(Transaction.class), anyString()))
                .thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> pscController.getPsc(
                transaction, PSC_ID, REQUEST_ID));
    }

    @Test
    void testCreatePscReturnsSuccess() throws Exception {
        when(pscService.createPsc(
                eq(transaction),
                any(PscDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(PSC_ID);

        var response = pscController.createPsc(
                transaction,
                pscDto,
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(String.format(URL_GET_PSC, TRANSACTION_ID, PSC_ID), responseHeaderLocation);
        PscSubmissionCreatedResponseDto responseBody = response.getBody();
        assert responseBody != null;
        assertEquals(PSC_ID, responseBody.id());

        assertTrue(StringUtils.isBlank(transaction.getResumeJourneyUri()));
    }

    @Test
    void testCreatePscThrowsServiceException() throws ServiceException {
        ServiceException exception = new ServiceException("Test");
        try (MockedStatic<ApiLogger> mockedLogger = Mockito.mockStatic(ApiLogger.class)) {
            doThrow(exception).when(pscService).createPsc(
                    eq(transaction),
                    any(PscDto.class),
                    eq(REQUEST_ID),
                    eq(USER_ID));

            var response = pscController.createPsc(
                    transaction,
                    pscDto,
                    REQUEST_ID,
                    USER_ID);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());

            mockedLogger.verify(() -> ApiLogger.errorContext(
                    eq(REQUEST_ID),
                    eq("Error creating the person with significant control"),
                    eq(exception),
                    any()
            ));
        }
    }
}
