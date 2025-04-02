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
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
class GeneralPartnerControllerTest {

    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String SUBMISSION_ID = "submission123";
    private static final String TRANSACTION_ID = "transaction123";
    private static final String GENERAL_PARTNER_ID = "abc-123";

    @InjectMocks
    private GeneralPartnerController generalPartnerController;

    @Mock
    private GeneralPartnerService generalPartnerService;

    @Mock
    private Transaction transaction;

    private GeneralPartnerDto generalPartnerDto;

    @BeforeEach
    void init() {
        GeneralPartnerDataDto data = new GeneralPartnerDataDto();
        generalPartnerDto = new GeneralPartnerDto();
        generalPartnerDto.setData(data);
    }

    @Test
    void testGetPartnerReturnsDto() throws ServiceException {
        GeneralPartnerDto dto = new GeneralPartnerDto();
        when(generalPartnerService.getGeneralPartner(any(Transaction.class), anyString()))
                .thenReturn(dto);

        var response = generalPartnerController.getGeneralPartner(transaction, SUBMISSION_ID, REQUEST_ID);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPartnerReturnsNotFoundWhenExceptionIsThrown() throws ServiceException {
        when(generalPartnerService.getGeneralPartner(any(Transaction.class), anyString()))
                .thenThrow(ResourceNotFoundException.class);

        var response = generalPartnerController.getGeneralPartner(transaction, SUBMISSION_ID, REQUEST_ID);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

    @Test
    void testCreatePartnerReturnsSuccess() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        when(generalPartnerService.createGeneralPartner(
                any(Transaction.class),
                any(GeneralPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(SUBMISSION_ID);

        when(transaction.getId()).thenReturn(TRANSACTION_ID);

        var response = generalPartnerController.createGeneralPartner(
                transaction,
                generalPartnerDto,
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(String.format(URL_GET_GENERAL_PARTNER, TRANSACTION_ID, SUBMISSION_ID),
                responseHeaderLocation);
        GeneralPartnerSubmissionCreatedResponseDto responseBody = response.getBody();
        assert responseBody != null;
        assertEquals(SUBMISSION_ID, responseBody.id());
    }

    @Test
    void testCreatePartnerReturnsException() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        when(generalPartnerService.createGeneralPartner(
                any(Transaction.class),
                any(GeneralPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID))).thenThrow(new ServiceException("Example"));

        var response = generalPartnerController.createGeneralPartner(
                transaction,
                generalPartnerDto,
                REQUEST_ID,
                USER_ID);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void testUpdatePartnerReturnsServiceException() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        doThrow(new ServiceException("Test")).when(generalPartnerService).updateGeneralPartner(
                eq(GENERAL_PARTNER_ID),
                any(GeneralPartnerDataDto.class),
                eq(REQUEST_ID),
                eq(USER_ID));

        var response = generalPartnerController.updateGeneralPartner(
                transaction,
                GENERAL_PARTNER_ID,
                new GeneralPartnerDataDto(),
                REQUEST_ID,
                USER_ID);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void testUpdatePartnerReturnsResourceNotFoundException() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        doThrow(new ResourceNotFoundException("Test")).when(generalPartnerService).updateGeneralPartner(
                eq(GENERAL_PARTNER_ID),
                any(GeneralPartnerDataDto.class),
                eq(REQUEST_ID),
                eq(USER_ID));

        var response = generalPartnerController.updateGeneralPartner(
                transaction,
                GENERAL_PARTNER_ID,
                new GeneralPartnerDataDto(),
                REQUEST_ID,
                USER_ID);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

    @Test
    void testValidationStatusWhenGeneralPartnerDataIsValid() throws ServiceException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(generalPartnerService.validateGeneralPartner(transaction, SUBMISSION_ID))
                .thenReturn(new ArrayList<>());

        // when
        var response = generalPartnerController.getValidationStatus(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        ValidationStatusResponse validationStatusResponse = response.getBody();
        assertEquals(true, validationStatusResponse.isValid());
        assertNull(validationStatusResponse.getValidationStatusError());
    }

    @Test
    void testValidationStatusWhenGeneralPartnerDataIsNotValid() throws ServiceException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        List errors = new ArrayList<ValidationStatusError>();
        errors.add(new ValidationStatusError("Forename is required", GeneralPartnerDataDto.FORENAME_FIELD, null, null));
        errors.add(new ValidationStatusError("Date of birth is required", GeneralPartnerDataDto.DATE_OF_BIRTH_FIELD, null, null));
        when(generalPartnerService.validateGeneralPartner(transaction, SUBMISSION_ID)).thenReturn(errors);

        // when
        var response = generalPartnerController.getValidationStatus(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        ValidationStatusResponse validationStatusResponse = response.getBody();

        assertThat(validationStatusResponse.getValidationStatusError())
                .hasSize(2)
                .satisfiesExactly(
                        validationStatusError -> assertThat(validationStatusError.getError()).isEqualTo("Forename is required"),
                        validationStatusError -> assertThat(validationStatusError.getError()).isEqualTo("Date of birth is required"));
    }

    @Test
    void testNotFoundReturnedWhenValidationStatusFailsToFindResource() throws ServiceException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(generalPartnerService.validateGeneralPartner(transaction, SUBMISSION_ID)).thenThrow(new ResourceNotFoundException("error"));

        // when
        var response = generalPartnerController.getValidationStatus(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testInternalServerErrorReturnedWhenValidationStatusEncountersServiceException() throws ServiceException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(generalPartnerService.validateGeneralPartner(transaction, SUBMISSION_ID)).thenThrow(new ServiceException("error"));

        // when
        var response = generalPartnerController.getValidationStatus(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }
}
