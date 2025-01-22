package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;

@ExtendWith(MockitoExtension.class)
class IncorporationControllerTest {

    @InjectMocks
    IncorporationController incorporationController;

    @Mock
    private LimitedPartnershipIncorporationService incorporationService;

    @Mock
    private Transaction transaction;

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = "ABC123ABC456";
    private static final String TRANSACTION_ID = "12321123";

    @Test
    void testCreateIncorporationIsSuccessful() throws ServiceException {
        // given
        when(incorporationService.createIncorporation(
                transaction, REQUEST_ID, USER_ID
        ))
                .thenReturn(SUBMISSION_ID);

        when(transaction.getId()).thenReturn(TRANSACTION_ID);

        // when
        var response = incorporationController.createIncorporation(
                transaction,
                REQUEST_ID,
                USER_ID);

        // then
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(
                String.format(URL_GET_INCORPORATION, TRANSACTION_ID, SUBMISSION_ID),
                responseHeaderLocation);
        LimitedPartnershipSubmissionCreatedResponseDto responseBody = (LimitedPartnershipSubmissionCreatedResponseDto) response.getBody();
        assert responseBody != null;
        assertEquals(SUBMISSION_ID, responseBody.id());
    }
}
