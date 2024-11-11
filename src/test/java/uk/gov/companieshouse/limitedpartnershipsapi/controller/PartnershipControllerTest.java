package uk.gov.companieshouse.limitedpartnershipsapi.controller;

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
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PartnershipControllerTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";

    @InjectMocks
    private PartnershipController partnershipController;

    @Mock
    private MockHttpServletRequest mockHttpServletRequest;

    private LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto;

    @BeforeEach
    void init() {
        DataDto data = new DataDto();
        limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        limitedPartnershipSubmissionDto.setData(data);

        mockHttpServletRequest = new MockHttpServletRequest();
    }

    @Test
    void testCreatePartnership() {
        var response = partnershipController.createPartnership(
                limitedPartnershipSubmissionDto,
                REQUEST_ID,
                USER_ID,
                mockHttpServletRequest);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals("/transactions/12321123/limited-partnership/partnership/3235233232", responseHeaderLocation);
        LimitedPartnershipSubmissionCreatedResponseDto responseBody = (LimitedPartnershipSubmissionCreatedResponseDto) response.getBody();
        assert responseBody != null;
        assertEquals("3235233232", responseBody.getId());
    }

    @Test
    void testCreatePartnershipInternalServerError() {
        try (MockedStatic<ApiLogger> logger = Mockito.mockStatic(ApiLogger.class)) {
            logger.when(() -> ApiLogger.infoContext(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap())).thenThrow(new RuntimeException());

            var response = partnershipController.createPartnership(
                    limitedPartnershipSubmissionDto,
                    REQUEST_ID,
                    USER_ID,
                    mockHttpServletRequest);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        }
    }
}
