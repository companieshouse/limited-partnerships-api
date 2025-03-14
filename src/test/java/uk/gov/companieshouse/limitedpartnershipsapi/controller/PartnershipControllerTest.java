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
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
class PartnershipControllerTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = "ABC123ABC456";
    private static final String TRANSACTION_ID = "12321123";

    @InjectMocks
    private PartnershipController partnershipController;

    @Mock
    private LimitedPartnershipService limitedPartnershipService;

    @Mock
    private Transaction transaction;

    private LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto;

    @BeforeEach
    void init() {
        DataDto data = new DataDto();
        limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        limitedPartnershipSubmissionDto.setData(data);
    }

    @Test
    void testCreatePartnershipIsSuccessful() throws ServiceException {
        // given
        when(limitedPartnershipService.createLimitedPartnership(
                any(Transaction.class),
                any(LimitedPartnershipSubmissionDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(SUBMISSION_ID);

        when(transaction.getId()).thenReturn(TRANSACTION_ID);

        // when
        var response = partnershipController.createPartnership(
                transaction,
                limitedPartnershipSubmissionDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(
                String.format(URL_GET_PARTNERSHIP, TRANSACTION_ID, SUBMISSION_ID),
                responseHeaderLocation);
        LimitedPartnershipSubmissionCreatedResponseDto responseBody = (LimitedPartnershipSubmissionCreatedResponseDto) response.getBody();
        assert responseBody != null;
        assertEquals(SUBMISSION_ID, responseBody.id());
    }

    @Test
    void testInternalServerErrorReturnedWhenCreatePartnershipFails() throws ServiceException {
        // given
        when(limitedPartnershipService.createLimitedPartnership(
                any(Transaction.class),
                any(LimitedPartnershipSubmissionDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenThrow(new ServiceException("TEST"));

        // when
        var response = partnershipController.createPartnership(
                transaction,
                limitedPartnershipSubmissionDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void testUpdatePartnershipIsSuccessful() throws ServiceException {
        // given
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();

        when(transaction.getId()).thenReturn(TRANSACTION_ID);

        // when
        var response = partnershipController.updatePartnership(
                transaction,
                SUBMISSION_ID,
                limitedPartnershipPatchDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        verify(limitedPartnershipService, times(1)).updateLimitedPartnership(
                transaction,
                SUBMISSION_ID,
                limitedPartnershipPatchDto,
                REQUEST_ID,
                USER_ID);
    }

    @Test
    void testInternalServerErrorReturnedWhenUpdatePartnershipFails() throws ServiceException {
        // given
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();

        doThrow(new ServiceException(String.format("Submission with id %s not found", SUBMISSION_ID)))
                .when(limitedPartnershipService).updateLimitedPartnership(
                        transaction,
                        SUBMISSION_ID,
                        limitedPartnershipPatchDto,
                        REQUEST_ID,
                        USER_ID);

        // when
        var response = partnershipController.updatePartnership(
                transaction,
                SUBMISSION_ID,
                limitedPartnershipPatchDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void testNotFoundReturnedWhenUpdatePartnershipFailsToFindResource() throws ServiceException {
        // given
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
        doThrow(new ResourceNotFoundException("error"))
                .when(limitedPartnershipService).updateLimitedPartnership(
                        transaction,
                        SUBMISSION_ID,
                        limitedPartnershipPatchDto,
                        REQUEST_ID,
                        USER_ID);

        // when
        var response = partnershipController.updatePartnership(
                transaction,
                SUBMISSION_ID,
                limitedPartnershipPatchDto,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testGetPartnershipIsSuccessful() throws ResourceNotFoundException {
        // given
        DataDto dataDto = new DataDto();
        dataDto.setPartnershipName("Test name");
        limitedPartnershipSubmissionDto.setData(dataDto);

        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(limitedPartnershipService.getLimitedPartnership(transaction, SUBMISSION_ID)).thenReturn(limitedPartnershipSubmissionDto);

        // when
        var response = partnershipController.getPartnership(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(limitedPartnershipSubmissionDto, response.getBody());
        assertEquals("Test name", limitedPartnershipSubmissionDto.getData().getPartnershipName());
        assertNull(limitedPartnershipSubmissionDto.getData().getNameEnding());
    }

    @Test
    void testNotFoundReturnedWhenGetPartnershipFailsToFindResource() throws ResourceNotFoundException {
        // given
        when(transaction.getId()).thenReturn(TRANSACTION_ID);
        when(limitedPartnershipService.getLimitedPartnership(transaction, SUBMISSION_ID)).thenThrow(new ResourceNotFoundException("error"));

        // when
        var response = partnershipController.getPartnership(
                transaction,
                SUBMISSION_ID,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
