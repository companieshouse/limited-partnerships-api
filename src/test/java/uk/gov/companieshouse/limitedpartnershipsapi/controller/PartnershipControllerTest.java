package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
class PartnershipControllerTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = "ABC123ABC456";

    @InjectMocks
    private PartnershipController partnershipController;

    @Mock
    private LimitedPartnershipService limitedPartnershipService;

    private LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto;

    @BeforeEach
    void init() {
        DataDto data = new DataDto();
        limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        limitedPartnershipSubmissionDto.setData(data);
    }

    @Test
    void testCreatePartnership() {
        when(limitedPartnershipService.createLimitedPartnership(
                any(LimitedPartnershipSubmissionDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(SUBMISSION_ID);

        var response = partnershipController.createPartnership(
                limitedPartnershipSubmissionDto,
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(
                String.format(URL_GET_PARTNERSHIP, 12321123, SUBMISSION_ID),
                responseHeaderLocation);
        LimitedPartnershipSubmissionCreatedResponseDto responseBody = (LimitedPartnershipSubmissionCreatedResponseDto) response.getBody();
        assert responseBody != null;
        assertEquals(SUBMISSION_ID, responseBody.getId());
    }

    @Test
    void testCreatePartnershipInternalServerError() {
        when(limitedPartnershipService.createLimitedPartnership(
                any(LimitedPartnershipSubmissionDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenThrow(new RuntimeException());

        var response = partnershipController.createPartnership(
                limitedPartnershipSubmissionDto,
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }
}
