package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.LimitedPartnerType.PERSON;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
class LimitedPartnerControllerTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = "ABC123ABC456";
    private static final String TRANSACTION_ID = "12321123";
    @InjectMocks
    LimitedPartnerController limitedPartnerController;

    @Mock
    private LimitedPartnerService limitedPartnerService;

    @Mock
    private Transaction transaction;

    private LimitedPartnerDto limitedPartnerDto;

    @BeforeEach
    void init() {
        LimitedPartnerDataDto data = new LimitedPartnerDataDto();
        limitedPartnerDto = new LimitedPartnerDto();
        limitedPartnerDto.setData(data);
    }

    @Test
    void testCreatePartnerIsSuccessful() throws ServiceException {
        when(limitedPartnerService.createLimitedPartner(
                any(Transaction.class),
                any(LimitedPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(SUBMISSION_ID);

        when(transaction.getId()).thenReturn(TRANSACTION_ID);

        // when
        var response = limitedPartnerController.createLimitedPartner(
                transaction,
                limitedPartnerDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(
                String.format(URL_GET_LIMITED_PARTNER, TRANSACTION_ID, SUBMISSION_ID),
                responseHeaderLocation);
        LimitedPartnerSubmissionCreatedResponseDto responseBody = response.getBody();
        assert responseBody != null;
        assertEquals(SUBMISSION_ID, responseBody.id());
    }

    @Test
    void givenServiceException_whenCreateLimitedPartner_thenInternalServerError() throws ServiceException {
        // given
        when(limitedPartnerService.createLimitedPartner(
                any(Transaction.class),
                any(LimitedPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenThrow(new ServiceException("TEST"));

        // when
        var response = limitedPartnerController.createLimitedPartner(
                transaction,
                limitedPartnerDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void testGetPartnerIsSuccessful() throws ResourceNotFoundException {
        // given
        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setPartnerType(PERSON);
        limitedPartnerDto.setData(limitedPartnerDataDto);

        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(limitedPartnerService.getLimitedPartner(transaction, SUBMISSION_ID)).thenReturn(limitedPartnerDto);

        // when
        var response = limitedPartnerController.getLimitedPartner(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(limitedPartnerDto, response.getBody());
    }

    @Test
    void testNotFoundReturnedWhenGetPartnerFailsToFindResource() throws ResourceNotFoundException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(limitedPartnerService.getLimitedPartner(transaction, SUBMISSION_ID)).thenThrow(new ResourceNotFoundException("error"));

        // when
        var response = limitedPartnerController.getLimitedPartner(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
