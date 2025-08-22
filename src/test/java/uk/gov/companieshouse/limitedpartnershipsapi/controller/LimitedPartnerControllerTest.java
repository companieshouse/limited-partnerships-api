package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder.LIMITED_PARTNER_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
class LimitedPartnerControllerTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    @InjectMocks
    private LimitedPartnerController limitedPartnerController;

    @Mock
    private LimitedPartnerService limitedPartnerService;

    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LIMITED_PARTNER_ID
    ).build();

    private LimitedPartnerDto limitedPartnerDto;

    @BeforeEach
    void init() {
        limitedPartnerDto = new LimitedPartnerBuilder().personDto();
    }

    @Test
    void testCreatePartnerIsSuccessful() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        when(limitedPartnerService.createLimitedPartner(
                any(Transaction.class),
                any(LimitedPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(SUBMISSION_ID);

        var response = limitedPartnerController.createLimitedPartner(
                transaction,
                limitedPartnerDto,
                REQUEST_ID,
                USER_ID);

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
    void givenServiceException_whenCreateLimitedPartner_thenInternalServerError() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        when(limitedPartnerService.createLimitedPartner(
                any(Transaction.class),
                any(LimitedPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenThrow(new ServiceException("TEST"));

        var response = limitedPartnerController.createLimitedPartner(
                transaction,
                limitedPartnerDto,
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void testGetPartnerIsSuccessful() throws ServiceException {
        LimitedPartnerDto dto = new LimitedPartnerDto();
        when(limitedPartnerService.getLimitedPartner(transaction, SUBMISSION_ID))
                .thenReturn(dto);

        var response = limitedPartnerController.getLimitedPartner(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testNotFoundReturnedWhenGetPartnerFailsToFindResource() throws ServiceException {
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(limitedPartnerService.getLimitedPartner(transaction, SUBMISSION_ID)).thenThrow(new ResourceNotFoundException("error"));

        var response = limitedPartnerController.getLimitedPartner(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testGetLimitedPartnersReturnsList() throws ServiceException {
        List<LimitedPartnerDto> limitedPartnerDtoList = List.of(new LimitedPartnerDto(), new LimitedPartnerDto());
        when(limitedPartnerService.getLimitedPartnerList(transaction)).thenReturn(limitedPartnerDtoList);

        var response = limitedPartnerController.getLimitedPartners(
                transaction,
                REQUEST_ID);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(limitedPartnerDtoList, response.getBody());
    }

    @Test
    void testGetLimitedPartnerReturnsEmptyList() throws ServiceException {
        List<LimitedPartnerDto> limitedPartnerDtoList = List.of();
        when(limitedPartnerService.getLimitedPartnerList(transaction)).thenReturn(limitedPartnerDtoList);

        var response = limitedPartnerController.getLimitedPartners(
                transaction,
                REQUEST_ID);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(limitedPartnerDtoList, response.getBody());
    }
}
