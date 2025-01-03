package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.sdk.impl.ApiClientServiceImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.CustomUserAuthenticationInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PartnershipController.class)
public class PartnershipControllerValidationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LimitedPartnershipService service;

    @MockBean
    private CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor;

    @MockBean
    private ApiClientServiceImpl apiClientServiceImpl;


    @Disabled("This test is disabled as work is in progress")
    @Test
    void testCreatePartnershipShouldReturnBadRequestErrorIfPartnershipNameIsLessThan1Character() throws Exception {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();

        DataDto dto = new DataDto();
        dto.setPartnershipName("");
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        limitedPartnershipSubmissionDto.setData(dto);

        String body = objectMapper.writeValueAsString(limitedPartnershipSubmissionDto);

        mvc.perform(post("/transactions/863851-951242-143528/limited-partnership/partnership")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }


}
