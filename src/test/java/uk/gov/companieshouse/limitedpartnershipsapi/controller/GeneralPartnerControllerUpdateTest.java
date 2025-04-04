package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GeneralPartnerController.class})
@WebMvcTest(controllers = {GeneralPartnerController.class})
class GeneralPartnerControllerUpdateTest {

    private static final String JSON_GENERAL_PARTNER_PERSON = """
            {
              "forename": "Joe",
              "surname": "Bloggs",
              "date_of_birth": "2001-01-01",
              "nationality1": "BRITISH",
              "nationality2": null
            }""";

    private static final String JSON_GENERAL_LEGAL_ENTITY = """
            {
                "legal_entity_name": "My Company ltd",
                "legal_form": "Limited Company",
                "governing_law": "Act of law",
                "legal_entity_register_name": "US Register",
                "legal_entity_registration_location": "United States",
                "registered_company_number": "12345678",
                "not_disqualified_statement_checked": true
            }""";

    private static final String JSON_WITH_BELOW_MIN_FORENAME = "{ \"forename\": \"\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

    private static final String JSON_WITH_ABOVE_MAX_SURNAME = "{ \"forename\": \"Joe\", \"surname\": \"The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

    private static final String JSON_INVALID_FORMER_NAMES = "{ \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

    private static final String JSON_INVALID_NATIONALITY = "{ \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"ABSURDISTANI\", \"nationality2\": null }";

    static String patchUrl = "/transactions/863851-951242-143528/limited-partnership/general-partner/3756304d-fa80-472a-bb6b-8f1f5f04d8eb";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeneralPartnerController generalPartnerController;

    @MockitoBean
    private GeneralPartnerService generalPartnerService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();

        this.mockMvc = MockMvcBuilders.standaloneSetup(generalPartnerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            JSON_GENERAL_PARTNER_PERSON,
            JSON_GENERAL_LEGAL_ENTITY
    })
    void shouldReturn200(String body) throws Exception {
        mockMvc.perform(patch(GeneralPartnerControllerUpdateTest.patchUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
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
        mockMvc.perform(patch(GeneralPartnerControllerUpdateTest.patchUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
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

        // service address
        private static final String JSON_SA_POSTCODE_EMPTY = "{\"service_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_POSTCODE_NOT_CORRECT = "{\"service_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_ADDRESS_LINE_1_TOO_SHORT = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

        private static final String JSON_SA_MISSING_POSTCODE = "{\"service_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_PREMISES = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_ADDRESS_LINE_1 = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_LOCALITY = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_COUNTRY = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_POA_UK,
                JSON_POA_NOT_UK,
                JSON_POA_NOT_UK_WITHOUT_POSTAL_CODE
        })
        void shouldReturn200(String body) throws Exception {
            mockMvc.perform(patch(GeneralPartnerControllerUpdateTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturn400IfAddressLine1IsTooLong() throws Exception {
            String longAddressLine1 = StringUtils.repeat("A", 51);
            String body = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"" + longAddressLine1 + "\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

            mockMvc.perform(patch(GeneralPartnerControllerUpdateTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
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
                JSON_SA_POSTCODE_EMPTY + "$ serviceAddress.postalCode $ Postcode must not be null",
                JSON_SA_POSTCODE_NOT_CORRECT + "$ serviceAddress.postalCode $ Invalid postcode format",
                JSON_SA_ADDRESS_LINE_1_TOO_SHORT + "$ serviceAddress.addressLine1 $ Address line 1 must be greater than 1"
        }, delimiter = '$')
        void shouldReturn400IfFieldIncorrect(String body, String field, String errorMessage) throws Exception {
            mockMvc.perform(patch(GeneralPartnerControllerUpdateTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
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
                JSON_SA_MISSING_POSTCODE + "$ serviceAddress.postalCode $ Postcode must not be null",
                JSON_SA_MISSING_PREMISES + "$ serviceAddress.premises $ Property name or number must not be null",
                JSON_SA_MISSING_ADDRESS_LINE_1 + "$ serviceAddress.addressLine1 $ Address line 1 must not be null",
                JSON_SA_MISSING_LOCALITY + "$ serviceAddress.locality $ Town or city must not be null",
                JSON_SA_MISSING_COUNTRY + "$ serviceAddress.country $ Country must not be null"
        }, delimiter = '$')
        void shouldReturn400IfRequiredFieldIsMissing(String body, String field, String errorMessage) throws Exception {
            mockMvc.perform(patch(GeneralPartnerControllerUpdateTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
        }
    }
}
