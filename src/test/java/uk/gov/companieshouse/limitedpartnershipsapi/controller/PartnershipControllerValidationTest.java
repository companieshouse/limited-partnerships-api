package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;

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

        private static final String JSON_MISSING_PARTNERSHIP_NAME = "{\"data\":{\"name_ending\":\"Limited Partnership\",\"partnership_type\":\"LP\"}}";
        private static final String JSON_INVALID_CHARS_PARTNERSHIP_NAME = "{\"data\":{\"partnership_name\":\"±±±Name test\", \"name_ending\":\"Limited Partnership\",\"partnership_type\":\"LP\"}}";
        private static final String JSON_MISSING_NAME_ENDING = "{\"data\":{\"partnership_name\":\"Name test\", \"partnership_type\":\"LP\"}}";
        private static final String JSON_MISSING_TYPE = "{\"data\":{\"partnership_name\":\"Name test\", \"name_ending\":\"Limited Partnership\"}}";


        @ParameterizedTest
        @ValueSource(strings = {
                JSON_MISSING_PARTNERSHIP_NAME,
                JSON_INVALID_CHARS_PARTNERSHIP_NAME,
                JSON_MISSING_NAME_ENDING,
                JSON_MISSING_TYPE
        })
        void shouldReturnBadRequest(String body) throws Exception {
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
        private static final String JSON_WITH_VALID_PARTNERSHIP_NAME = "{\"partnership_name\":\"Correct name size\",\"name_ending\":\"Limited Partnership\"}";
        private static final String JSON_WITH_VALID_JURISDICTION = "{\"jurisdiction\":\"Scotland\"}";
        private static final String JSON_WITH_VALID_NULL_JURISDICTION = "{\"jurisdiction\":null}";

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_WITH_VALID_PARTNERSHIP_NAME,
                JSON_WITH_VALID_JURISDICTION,
                JSON_WITH_VALID_NULL_JURISDICTION
        })
        void shouldReturn200(String body) throws Exception {
            mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Nested
        class Email {
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
        class NameInvalidChars {
            @Test
            void shouldReturnBadRequestErrorIfNameContainsInvalidChars() throws Exception {
                String body = "{\"partnership_name\":\"±±\",\"name_ending\":\"Limited Partnership\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.partnershipName").value("Limited partnership name " + INVALID_CHARACTERS_MESSAGE));
            }
        }

        @Nested
        class Jurisdiction {
            @Test
            void testUpdatePartnershipWithAnInvalidJurisdictionShouldReturn400() throws Exception {
                String body = "{\"jurisdiction\":\"Croatia\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.jurisdiction").value("Jurisdiction must be valid"));
            }
        }

        @Nested
        class Addresses {
            // registered_office_address
            private static final String JSON_ROA_POSTCODE_EMPTY = "{\"registered_office_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_POSTCODE_NOT_CORRECT = "{\"registered_office_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_ADDRESS_LINE_1_TOO_SHORT = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

            private static final String JSON_ROA_MISSING_POSTCODE = "{\"registered_office_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_MISSING_PREMISES = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_MISSING_ADDRESS_LINE_1 = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_MISSING_LOCALITY = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_MISSING_COUNTRY = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

            private static final String JSON_ROA_PREMISES_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"±\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_ADDRESS_LINE_1_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"±±DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_ADDRESS_LINE_2_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"±±\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_ADDRESS_LOCALITY_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"±±STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_ADDRESS_REGION_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"±±Herefordshire\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_ROA_ADDRESS_POSTCODE_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"±±ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"Herefordshire\", \"country\":\"GB-ENG\"}}";

            // principal place of business
            private static final String JSON_PPOB_POSTCODE_EMPTY = "{\"principal_place_of_business_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_POSTCODE_NOT_CORRECT = "{\"principal_place_of_business_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_ADDRESS_LINE_1_TOO_SHORT = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

            private static final String JSON_PPOB_MISSING_POSTCODE = "{\"principal_place_of_business_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_MISSING_PREMISES = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_MISSING_ADDRESS_LINE_1 = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_MISSING_LOCALITY = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_MISSING_COUNTRY = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

            private static final String JSON_PPOB_PREMISES_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"±\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_ADDRESS_LINE_1_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"±±DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_ADDRESS_LINE_2_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"±±\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_ADDRESS_LOCALITY_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"±±STOKE-ON-TRENT\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_ADDRESS_REGION_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"±±Herefordshire\", \"country\":\"GB-ENG\"}}";
            private static final String JSON_PPOB_ADDRESS_POSTCODE_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"±±ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"Herefordshire\", \"country\":\"GB-ENG\"}}";

            @Test
            void shouldReturn200() throws Exception {
                String body = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
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
                String body = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"" + longAddressLine1 + "\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"GB-ENG\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    JSON_ROA_POSTCODE_EMPTY,
                    JSON_ROA_POSTCODE_NOT_CORRECT,
                    JSON_ROA_ADDRESS_LINE_1_TOO_SHORT,
                    JSON_PPOB_POSTCODE_EMPTY,
                    JSON_PPOB_POSTCODE_NOT_CORRECT,
                    JSON_PPOB_ADDRESS_LINE_1_TOO_SHORT
            })
            void shouldReturn400IfFieldIncorrect(String body) throws Exception {
                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    JSON_ROA_MISSING_POSTCODE,
                    JSON_ROA_MISSING_PREMISES,
                    JSON_ROA_MISSING_ADDRESS_LINE_1,
                    JSON_ROA_MISSING_LOCALITY,
                    JSON_ROA_MISSING_COUNTRY,
                    JSON_PPOB_MISSING_POSTCODE,
                    JSON_PPOB_MISSING_PREMISES,
                    JSON_PPOB_MISSING_ADDRESS_LINE_1,
                    JSON_PPOB_MISSING_LOCALITY,
                    JSON_PPOB_MISSING_COUNTRY
            })
            void shouldReturn400IfRequiredFieldIsMissing(String body) throws Exception {
                    mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .characterEncoding("utf-8")
                                    .headers(httpHeaders)
                                    .requestAttr("transaction", transaction)
                                    .content(body))
                            .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @MethodSource("provideInvalidCharsInputsAndMessages")
            void shouldReturn400IfFieldHasInvalidChars(String body, String fieldName, String expectedErrorMessage) throws Exception {
                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath(fieldName).value(expectedErrorMessage));
            }

            private static Stream<Arguments> provideInvalidCharsInputsAndMessages() {
                return Stream.of(
                        Arguments.of(JSON_ROA_PREMISES_INVALID_CHARS,
                                "$.[\"errors\"].[\"registeredOfficeAddress.premises\"]",
                                "Property name or number " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_ROA_ADDRESS_LINE_1_INVALID_CHARS,
                                "$.[\"errors\"].[\"registeredOfficeAddress.addressLine1\"]",
                                "Address line 1 " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_ROA_ADDRESS_LINE_2_INVALID_CHARS,
                                "$.[\"errors\"].[\"registeredOfficeAddress.addressLine2\"]",
                                "Address line 2 " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_ROA_ADDRESS_LOCALITY_INVALID_CHARS,
                                "$.[\"errors\"].[\"registeredOfficeAddress.locality\"]",
                                "Town or city " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_ROA_ADDRESS_REGION_INVALID_CHARS,
                                "$.[\"errors\"].[\"registeredOfficeAddress.region\"]",
                                "County " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_ROA_ADDRESS_POSTCODE_INVALID_CHARS,
                                "$.[\"errors\"].[\"registeredOfficeAddress.postalCode\"]",
                                "Invalid postcode format"),
                        Arguments.of(JSON_PPOB_PREMISES_INVALID_CHARS,
                                "$.[\"errors\"].[\"principalPlaceOfBusinessAddress.premises\"]",
                                "Property name or number " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_PPOB_ADDRESS_LINE_1_INVALID_CHARS,
                                "$.[\"errors\"].[\"principalPlaceOfBusinessAddress.addressLine1\"]",
                                "Address line 1 " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_PPOB_ADDRESS_LINE_2_INVALID_CHARS,
                                "$.[\"errors\"].[\"principalPlaceOfBusinessAddress.addressLine2\"]",
                                "Address line 2 " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_PPOB_ADDRESS_LOCALITY_INVALID_CHARS,
                                "$.[\"errors\"].[\"principalPlaceOfBusinessAddress.locality\"]",
                                "Town or city " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_PPOB_ADDRESS_REGION_INVALID_CHARS,
                                "$.[\"errors\"].[\"principalPlaceOfBusinessAddress.region\"]",
                                "County " + INVALID_CHARACTERS_MESSAGE),
                        Arguments.of(JSON_PPOB_ADDRESS_POSTCODE_INVALID_CHARS,
                                "$.[\"errors\"].[\"principalPlaceOfBusinessAddress.postalCode\"]",
                                "Invalid postcode format")
                );
            }
        }

        @Nested
        class Term {
            private static final String JSON_TERM_DECIDED = "{\"term\":\"BY_AGREEMENT\"}";
            private static final String JSON_TERM_DISSOLVED = "{\"term\":\"UNTIL_DISSOLUTION\"}";
            private static final String JSON_TERM_NONE = "{\"term\":\"NONE\"}";

            @ParameterizedTest
            @ValueSource(strings = {
                    JSON_TERM_DECIDED,
                    JSON_TERM_DISSOLVED,
                    JSON_TERM_NONE,
            })
            void shouldReturn200(String body) throws Exception {
                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturn400IfTermIncorrect() throws Exception {
                String body = "{\"term\":\"wrong-term\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.patchUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.term").value("Term must be valid"));
            }
        }
    }
}
