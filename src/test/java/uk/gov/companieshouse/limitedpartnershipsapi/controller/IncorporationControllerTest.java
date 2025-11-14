package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

@ExtendWith(MockitoExtension.class)
class IncorporationControllerTest {

    @InjectMocks
    private IncorporationController incorporationController;

    @Mock
    private LimitedPartnershipIncorporationService incorporationService;

    private final Transaction transaction = new TransactionBuilder().build();

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    @Test
    void testCreateIncorporationIsSuccessful() throws ServiceException {
        // given
        IncorporationDto incorporationDto = new IncorporationDto();
        IncorporationDataDto dataDto = new IncorporationDataDto();
        incorporationDto.setData(dataDto);

        when(incorporationService.createIncorporation(
                transaction, incorporationDto, REQUEST_ID, USER_ID
        ))
                .thenReturn(SUBMISSION_ID);

        // when
        var response = incorporationController.createIncorporation(
                transaction,
                incorporationDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(
                String.format(URL_GET_INCORPORATION, TRANSACTION_ID, SUBMISSION_ID),
                responseHeaderLocation);
        LimitedPartnershipCreatedResponseDto responseBody = (LimitedPartnershipCreatedResponseDto) response.getBody();
        assert responseBody != null;
        assertEquals(SUBMISSION_ID, responseBody.id());
    }

    @Test
    void testGetIncorporationIsSuccessful() throws ServiceException {
        // given
        LimitedPartnershipIncorporationDto limitedPartnershipIncorporationDto = new LimitedPartnershipIncorporationDto();

        limitedPartnershipIncorporationDto.setKind(REGISTRATION.getDescription());

        when(incorporationService.getIncorporation(transaction, SUBMISSION_ID, true)).thenReturn(limitedPartnershipIncorporationDto);

        // when
        var response = incorporationController.getIncorporation(
                transaction,
                SUBMISSION_ID,
                true,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(limitedPartnershipIncorporationDto, response.getBody());
        assertEquals(REGISTRATION.getDescription(), limitedPartnershipIncorporationDto.getKind());
    }

    @Test
    void testNotFoundReturnedWhenGetIncorporationFailsToFindResource() throws ServiceException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(incorporationService.getIncorporation(transaction, SUBMISSION_ID, true)).thenThrow(new ResourceNotFoundException("error"));

        // when
        var response = incorporationController.getIncorporation(
                transaction,
                SUBMISSION_ID,
                true,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testInternalServerErrorReturnedWhenGetIncorporationFailsWithServiceException() throws ServiceException {
        // given
        when(incorporationService.getIncorporation(transaction, SUBMISSION_ID, true)).thenThrow(new ServiceException("error"));

        // when
        var response = incorporationController.getIncorporation(
                transaction,
                SUBMISSION_ID,
                true,
                REQUEST_ID);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
