package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PartnershipController.class})
@WebMvcTest(controllers = {PartnershipController.class})
class PartnershipControllerValidationTest {

    static String postUrl = "/transactions/863851-951242-143528/limited-partnership/partnership";
    static String patchUrl = postUrl + "/93702824-9062-4c63-a694-716acffccdd5";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionInterceptor transactionInterceptor;

    @Autowired
    private PartnershipController partnershipController;

    @MockBean
    private LimitedPartnershipService service;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();

        this.mockMvc = MockMvcBuilders.standaloneSetup(partnershipController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

    }

    @Nested
    class CreatePartnership {
        @Test
        void shouldReturn201() throws Exception {

            LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
            DataDto dto = new DataDto();

            dto.setPartnershipName("test name");
            dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
            dto.setPartnershipType(PartnershipType.LP);
            limitedPartnershipSubmissionDto.setData(dto);

            String body = objectMapper.writeValueAsString(limitedPartnershipSubmissionDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.postUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        void shouldReturnBadRequestErrorIfPartnershipNameIsLessThan1Character() throws Exception {

            LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();

            DataDto dto = new DataDto();
            dto.setPartnershipName("");
            dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
            dto.setPartnershipType(PartnershipType.LP);
            limitedPartnershipSubmissionDto.setData(dto);

            String body = objectMapper.writeValueAsString(limitedPartnershipSubmissionDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.postUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class UpdatePartnership {
        @Test
        void shouldReturn200() throws Exception {
            String body = "{\"email\":\"test@email.com\"}";

            mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnBadRequestErrorIfEmailBadlyFormated() throws Exception {
            String body = "{\"email\":\"test@email.\"}";

            mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Nested
        class NameSize {
            @Test
            void shouldReturn200IfNameSizeIsCorrect() throws Exception {
                String body = "{\"partnership_name\":\"Correct name size\",\"name_ending\":\"Limited Partnership\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturnBadRequestErrorIfNameSizeIsTooLong() throws Exception {
                String longName = StringUtils.repeat("A", 160);
                String body = "{\"partnership_name\":\"" + longName + "\",\"name_ending\":\"Limited Partnership\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        class RegisteredOfficeAddress {
            @Test
            void shouldReturn200() throws Exception {
                String body = "{\"registered_office_address\":{\"postcode\":\"ST6 3LJ\",\"premise\":\"2\",\"addressLine1\":\"DUNCALF STREET\",\"addressLine2\":\"\",\"postTown\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andDo(print())
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturn400IfPostCodeIsMissing() throws Exception {
                String body = "{\"registered_office_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"region\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn400IfPostCodeIsNotCorrect() throws Exception {
                String body = "{\"registered_office_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"region\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn400IfAddressLine1IsTooShort() throws Exception {
                String body = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"region\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn400IfAddressLine1IsTooLong() throws Exception {
                String longAddressLine1 = StringUtils.repeat("A", 51);
                String body = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"" + longAddressLine1 + "\",\"address_line_2\":\"\",\"region\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
