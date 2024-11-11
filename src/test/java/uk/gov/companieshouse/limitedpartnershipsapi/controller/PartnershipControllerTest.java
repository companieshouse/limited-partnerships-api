package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionCreatedResponseDto;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PartnershipControllerTest {

    @InjectMocks
    private PartnershipController partnershipController;

    @Mock
    private MockHttpServletRequest mockHttpServletRequest;

    @Test
    void testCreatePartnership() {
        var response = partnershipController.createPartnership(mockHttpServletRequest);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals("/transactions/12321123/limited-partnership/partnership/3235233232", responseHeaderLocation);
        LimitedPartnershipSubmissionCreatedResponseDto responseBody = (LimitedPartnershipSubmissionCreatedResponseDto) response.getBody();
        assert responseBody != null;
        assertEquals("3235233232", responseBody.getId());
    }
}
