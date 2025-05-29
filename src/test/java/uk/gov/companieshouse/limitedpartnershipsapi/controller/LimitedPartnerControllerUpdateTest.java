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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ContextConfiguration(classes = {LimitedPartnerController.class, LimitedPartnerService.class, LimitedPartnerValidator.class, LimitedPartnerMapperImpl.class, CostsService.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {LimitedPartnerController.class})
public class LimitedPartnerControllerUpdateTest {
    private static final String TRANSACTION_ID = "863851-951242-143528";
    private static final String LIMITED_PARTNER_ID = LimitedPartnerBuilder.LIMITED_PARTNER_ID;
    private static final String LIMITED_PARTNER_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID;
    private static final String LIMITED_PARTNER_COST_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID + "/costs";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().build(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LIMITED_PARTNER_ID
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionUtils transactionUtils;

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
                    "registered_company_number": "12345678",
                    "legal_personality_statement_checked": true
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
            mocks();

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
                JSON_WITH_BELOW_MIN_FORENAME + "$ data.forename $ Forename must be greater than 1",
                JSON_WITH_ABOVE_MAX_SURNAME + "$ data.surname $ Surname must be less than 160",
                JSON_INVALID_FORMER_NAMES + "$ data.formerNames $ Former names " + INVALID_CHARACTERS_MESSAGE,
                JSON_INVALID_NATIONALITY + "$ data.nationality1 $ First nationality must be valid"
        }, delimiter = '$')
        void shouldReturn400(String body, String field, String errorMessage) throws Exception {
            mocks();

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
                mocks();

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
                mocks();

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
                    JSON_POA_POSTCODE_EMPTY + "$ data.principalOfficeAddress.postalCode $ Postcode must not be null",
                    JSON_POA_POSTCODE_NOT_CORRECT + "$ data.principalOfficeAddress.postalCode $ Invalid postcode format",
                    JSON_POA_ADDRESS_LINE_1_TOO_SHORT + "$ data.principalOfficeAddress.addressLine1 $ Address line 1 must be greater than 1",
                    JSON_URA_POSTCODE_EMPTY + "$ data.usualResidentialAddress.postalCode $ Postcode must not be null",
                    JSON_URA_POSTCODE_NOT_CORRECT + "$ data.usualResidentialAddress.postalCode $ Invalid postcode format",
                    JSON_URA_ADDRESS_LINE_1_TOO_SHORT + "$ data.usualResidentialAddress.addressLine1 $ Address line 1 must be greater than 1"
            }, delimiter = '$')
            void shouldReturn400IfFieldIncorrect(String body, String field, String errorMessage) throws Exception {
                mocks();

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
                    JSON_POA_MISSING_POSTCODE + "$ data.principalOfficeAddress.postalCode $ Postcode must not be null",
                    JSON_POA_MISSING_PREMISES + "$ data.principalOfficeAddress.premises $ Property name or number must not be null",
                    JSON_POA_MISSING_ADDRESS_LINE_1 + "$ data.principalOfficeAddress.addressLine1 $ Address line 1 must not be null",
                    JSON_POA_MISSING_LOCALITY + "$ data.principalOfficeAddress.locality $ Town or city must not be null",
                    JSON_POA_MISSING_COUNTRY + "$ data.principalOfficeAddress.country $ Country must not be null",
                    JSON_URA_MISSING_POSTCODE + "$ data.usualResidentialAddress.postalCode $ Postcode must not be null",
                    JSON_URA_MISSING_PREMISES + "$ data.usualResidentialAddress.premises $ Property name or number must not be null",
                    JSON_URA_MISSING_ADDRESS_LINE_1 + "$ data.usualResidentialAddress.addressLine1 $ Address line 1 must not be null",
                    JSON_URA_MISSING_LOCALITY + "$ data.usualResidentialAddress.locality $ Town or city must not be null",
                    JSON_URA_MISSING_COUNTRY + "$ data.usualResidentialAddress.country $ Country must not be null"
            }, delimiter = '$')
            void shouldReturn400IfRequiredFieldIsMissing(String body, String field, String errorMessage) throws Exception {
                mocks();

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
            mocks();

            mockMvc.perform(delete(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404() throws Exception {
            mocks();

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.empty());

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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].amount").value("0.00"))
                    .andExpect(jsonPath("$.[0].description").value("Limited Partner fee"));
        }
    }

    private void mocks(LimitedPartnerDao limitedPartnerDao) {
        when(limitedPartnerRepository.insert((LimitedPartnerDao) any())).thenReturn(limitedPartnerDao);
        when(limitedPartnerRepository.save(any())).thenReturn(limitedPartnerDao);
        when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));
        doNothing().when(limitedPartnerRepository).deleteById(LIMITED_PARTNER_ID);

        when(transactionUtils.isTransactionLinkedToPartnerSubmission(any(), any(), any())).thenReturn(true);
    }

    private void mocks() {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().dao();

        mocks(limitedPartnerDao);
    }
}
