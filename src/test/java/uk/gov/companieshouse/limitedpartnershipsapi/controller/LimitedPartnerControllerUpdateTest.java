package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;

@ContextConfiguration(classes = {LimitedPartnerController.class, CostsService.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {LimitedPartnerController.class})
public class LimitedPartnerControllerUpdateTest {
    private static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private static final String LIMITED_PARTNER_URL = "/transactions/863851-951242-143528/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID;
    private static final String LIMITED_PARTNER_COST_URL = "/transactions/863851-951242-143528/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID + "/costs";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnerService limitedPartnerService;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private LimitedPartnershipIncorporationRepository limitedPartnershipIncorporationRepository;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();
    }

    @Nested
    class UpdateLimitedPartner {

        private static final String JSON_LIMITED_PARTNER_PERSON = """
                {
                  "forename": "Joe",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null
                }""";

        private static final String JSON_LIMITED_LEGAL_ENTITY = """
                {
                    "legal_entity_name": "My Company ltd",
                    "legal_form": "Limited Company",
                    "governing_law": "Act of law",
                    "legal_entity_register_name": "US Register",
                    "legal_entity_registration_location": "United States",
                    "registered_company_number": "12345678"
                }""";

        private static final String JSON_WITH_BELOW_MIN_FORENAME = "{ \"forename\": \"\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

        private static final String JSON_WITH_ABOVE_MAX_SURNAME = "{ \"forename\": \"Joe\", \"surname\": \"The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

        private static final String JSON_INVALID_FORMER_NAMES = "{ \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

        private static final String JSON_INVALID_NATIONALITY = "{ \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"ABSURDISTANI\", \"nationality2\": null }";

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_LIMITED_PARTNER_PERSON,
                JSON_LIMITED_LEGAL_ENTITY
        })
        void shouldReturn200(String body) throws Exception {
            mockMvc.perform(patch(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @CsvSource(value = {
                JSON_WITH_BELOW_MIN_FORENAME + "$ forename $ Forename must be greater than 1",
                JSON_WITH_ABOVE_MAX_SURNAME + "$ surname $ Surname must be less than 160",
                JSON_INVALID_FORMER_NAMES + "$ formerNames $ Former names " + INVALID_CHARACTERS_MESSAGE,
                JSON_INVALID_NATIONALITY + "$ nationality1 $ First nationality must be valid"
        }, delimiter = '$')
        void shouldReturn400(String body, String field, String errorMessage) throws Exception {
            mockMvc.perform(patch(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage))
                    .andExpect(status().isBadRequest());
        }

        @Nested
        class Addresses {
            // correct addresses
            private static final String JSON_POA_UK = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_POA_NOT_UK = "{\"principal_office_address\":{\"postal_code\":\"12345\",\"premises\":\"2\",\"address_line_1\":\"test rue\",\"address_line_2\":\"\",\"locality\":\"TOULOUSE\",\"country\":\"France\"}}";
            private static final String JSON_POA_NOT_UK_WITHOUT_POSTAL_CODE = "{\"principal_office_address\":{\"premises\":\"2\",\"address_line_1\":\"test rue\",\"address_line_2\":\"\",\"locality\":\"TOULOUSE\",\"country\":\"France\"}}";

            // principal office address
            private static final String JSON_POA_POSTCODE_EMPTY = "{\"principal_office_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_POA_POSTCODE_NOT_CORRECT = "{\"principal_office_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_POA_ADDRESS_LINE_1_TOO_SHORT = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

            private static final String JSON_POA_MISSING_POSTCODE = "{\"principal_office_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_POA_MISSING_PREMISES = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_POA_MISSING_ADDRESS_LINE_1 = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_POA_MISSING_LOCALITY = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
            private static final String JSON_POA_MISSING_COUNTRY = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

            // usual residential address
            private static final String JSON_URA_POSTCODE_EMPTY = "{\"usual_residential_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_URA_POSTCODE_NOT_CORRECT = "{\"usual_residential_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_URA_ADDRESS_LINE_1_TOO_SHORT = "{\"usual_residential_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

            private static final String JSON_URA_MISSING_POSTCODE = "{\"usual_residential_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_URA_MISSING_PREMISES = "{\"usual_residential_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_URA_MISSING_ADDRESS_LINE_1 = "{\"usual_residential_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_URA_MISSING_LOCALITY = "{\"usual_residential_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
            private static final String JSON_URA_MISSING_COUNTRY = "{\"usual_residential_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

            @ParameterizedTest
            @ValueSource(strings = {
                    JSON_POA_UK,
                    JSON_POA_NOT_UK,
                    JSON_POA_NOT_UK_WITHOUT_POSTAL_CODE
            })
            void shouldReturn200(String body) throws Exception {
                mockMvc.perform(patch(LIMITED_PARTNER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturn400IfAddressLine1IsTooLong() throws Exception {
                String longAddressLine1 = StringUtils.repeat("A", 51);
                String body = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"" + longAddressLine1 + "\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

                mockMvc.perform(patch(LIMITED_PARTNER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @CsvSource(value = {
                    JSON_POA_POSTCODE_EMPTY + "$ principalOfficeAddress.postalCode $ Postcode must not be null",
                    JSON_POA_POSTCODE_NOT_CORRECT + "$ principalOfficeAddress.postalCode $ Invalid postcode format",
                    JSON_POA_ADDRESS_LINE_1_TOO_SHORT + "$ principalOfficeAddress.addressLine1 $ Address line 1 must be greater than 1",
                    JSON_URA_POSTCODE_EMPTY + "$ usualResidentialAddress.postalCode $ Postcode must not be null",
                    JSON_URA_POSTCODE_NOT_CORRECT + "$ usualResidentialAddress.postalCode $ Invalid postcode format",
                    JSON_URA_ADDRESS_LINE_1_TOO_SHORT + "$ usualResidentialAddress.addressLine1 $ Address line 1 must be greater than 1"
            }, delimiter = '$')
            void shouldReturn400IfFieldIncorrect(String body, String field, String errorMessage) throws Exception {
                mockMvc.perform(patch(LIMITED_PARTNER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
            }

            @ParameterizedTest
            @CsvSource(value = {
                    JSON_POA_MISSING_POSTCODE + "$ principalOfficeAddress.postalCode $ Postcode must not be null",
                    JSON_POA_MISSING_PREMISES + "$ principalOfficeAddress.premises $ Property name or number must not be null",
                    JSON_POA_MISSING_ADDRESS_LINE_1 + "$ principalOfficeAddress.addressLine1 $ Address line 1 must not be null",
                    JSON_POA_MISSING_LOCALITY + "$ principalOfficeAddress.locality $ Town or city must not be null",
                    JSON_POA_MISSING_COUNTRY + "$ principalOfficeAddress.country $ Country must not be null",
                    JSON_URA_MISSING_POSTCODE + "$ usualResidentialAddress.postalCode $ Postcode must not be null",
                    JSON_URA_MISSING_PREMISES + "$ usualResidentialAddress.premises $ Property name or number must not be null",
                    JSON_URA_MISSING_ADDRESS_LINE_1 + "$ usualResidentialAddress.addressLine1 $ Address line 1 must not be null",
                    JSON_URA_MISSING_LOCALITY + "$ usualResidentialAddress.locality $ Town or city must not be null",
                    JSON_URA_MISSING_COUNTRY + "$ usualResidentialAddress.country $ Country must not be null"
            }, delimiter = '$')
            void shouldReturn400IfRequiredFieldIsMissing(String body, String field, String errorMessage) throws Exception {
                mockMvc.perform(patch(LIMITED_PARTNER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
            }
        }
    }

    @Nested
    class DeleteLimitedPartner {
        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Limited partner with id %s not found " + LIMITED_PARTNER_ID))
                    .when(limitedPartnerService)
                    .deleteLimitedPartner(any(), any(), any());

            mockMvc.perform(delete(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Costs {

        @Test
        void shouldReturn200() throws Exception {
            mockMvc.perform(get(LIMITED_PARTNER_COST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].amount").value("0.00"))
                    .andExpect(jsonPath("$.[0].description").value("Limited Partner fee"));
        }
    }
}
