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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testGetPartnerThrowsResourceNotFoundException() throws ServiceException {
        when(generalPartnerService.getGeneralPartner(any(Transaction.class), anyString()))
                .thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> generalPartnerController.getGeneralPartner(
                transaction, SUBMISSION_ID, REQUEST_ID));
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
    void testCreatePartnerThrowsServiceException() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        when(generalPartnerService.createGeneralPartner(
                any(Transaction.class),
                any(GeneralPartnerDto.class),
                eq(REQUEST_ID),
                eq(USER_ID))).thenThrow(new ServiceException("Example"));

        assertThrows(ServiceException.class, () -> generalPartnerController.createGeneralPartner(
                transaction,
                generalPartnerDto,
                REQUEST_ID,
                USER_ID));
    }

    @Test
    void testUpdatePartnerThrowsServiceException() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        doThrow(new ServiceException("Test")).when(generalPartnerService).updateGeneralPartner(
                eq(transaction),
                eq(GENERAL_PARTNER_ID),
                any(GeneralPartnerDataDto.class),
                eq(REQUEST_ID),
                eq(USER_ID));

        assertThrows(ServiceException.class, () -> generalPartnerController.updateGeneralPartner(
                transaction,
                GENERAL_PARTNER_ID,
                new GeneralPartnerDataDto(),
                REQUEST_ID,
                USER_ID));
    }

    @Test
    void testUpdatePartnerThrowsResourceNotFoundException() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        doThrow(new ResourceNotFoundException("Test")).when(generalPartnerService).updateGeneralPartner(
                eq(transaction),
                eq(GENERAL_PARTNER_ID),
                any(GeneralPartnerDataDto.class),
                eq(REQUEST_ID),
                eq(USER_ID));

        assertThrows(ResourceNotFoundException.class, () -> generalPartnerController.updateGeneralPartner(
                transaction,
                GENERAL_PARTNER_ID,
                new GeneralPartnerDataDto(),
                REQUEST_ID,
                USER_ID));
    }

    @Test
    void testGetGeneralPartnersReturnsList() {
        List<GeneralPartnerDto> generalPartnerDtoList = List.of(new GeneralPartnerDto(), new GeneralPartnerDto());
        when(generalPartnerService.getGeneralPartnerList(transaction)).thenReturn(generalPartnerDtoList);

        var response = generalPartnerController.getGeneralPartners(transaction, REQUEST_ID);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(generalPartnerDtoList, response.getBody());
    }
}
