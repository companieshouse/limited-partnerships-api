package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipPatchMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.LimitedPartnershipValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategyHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;

@ContextConfiguration(classes = {
        PartnershipController.class,
        LimitedPartnershipService.class,
        LimitedPartnershipValidator.class,
        PostTransitionStrategyHandler.class,
        Validator.class,
        ValidationStatus.class,
        LimitedPartnershipMapperImpl.class,
        LimitedPartnershipPatchMapperImpl.class,
        CostsService.class,
        GlobalExceptionHandler.class
})
@WebMvcTest(controllers = {PartnershipController.class})
class PartnershipControllerValidationTest {
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String SUBMISSION_ID = TransactionBuilder.SUBMISSION_ID;

    private static final String POST_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/partnership";
    private static final String PATCH_URL = POST_URL + "/" + SUBMISSION_ID;
    private static final String VALIDATE_STATUS_URL = PATCH_URL + "/validation-status";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LimitedPartnershipRepository repository;

    @MockitoBean
    private LimitedPartnershipIncorporationRepository limitedPartnershipIncorporationRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new TransactionBuilder().build();
        transaction.setFilingMode("limited-partnership-registration");
    }

    @Nested
    class CreatePartnership {
        @Test
        void shouldReturn201() throws Exception {
            mocks();
            transaction.getResources().clear();

            LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
            DataDto dto = new DataDto();

            dto.setPartnershipName("test name");
            dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
            dto.setPartnershipType(PartnershipType.LP);
            limitedPartnershipDto.setData(dto);

            String body = objectMapper.writeValueAsString(limitedPartnershipDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        void shouldReturnBadRequestErrorIfPartnershipNameIsLessThan1Character() throws Exception {
            LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();

            DataDto dto = new DataDto();
            dto.setPartnershipName("");
            dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
            dto.setPartnershipType(PartnershipType.LP);
            limitedPartnershipDto.setData(dto);

            String body = objectMapper.writeValueAsString(limitedPartnershipDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestErrorIfNameEndingForRegistrationIsMissing() throws Exception {
            mocks();
            transaction.getResources().clear();
            transaction.setFilingMode("limited-partnership-registration");

            LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();
            limitedPartnershipDto.getData().setNameEnding(null);

            String body = objectMapper.writeValueAsString(limitedPartnershipDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn201IfNameEndingForTransitionIsMissing() throws Exception {
            mocks();
            transaction.getResources().clear();
            transaction.setFilingMode("limited-partnership-transition");

            LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();
            limitedPartnershipDto.getData().setNameEnding(null);
            limitedPartnershipDto.getData().setPartnershipNumber("LP121212");

            String body = objectMapper.writeValueAsString(limitedPartnershipDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        void shouldReturnBadRequestErrorIfCompanyNumberForTransitionIsInvalid() throws Exception {
            mocks();
            transaction.getResources().clear();
            transaction.setFilingMode("limited-partnership-transition");

            LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();
            limitedPartnershipDto.getData().setNameEnding(null);
            limitedPartnershipDto.getData().setPartnershipNumber("LP1212");

            String body = objectMapper.writeValueAsString(limitedPartnershipDto);

            mockMvc.perform(post(PartnershipControllerValidationTest.POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        private static final String JSON_MISSING_PARTNERSHIP_NAME = "{\"data\":{\"name_ending\":\"Limited Partnership\",\"partnership_type\":\"LP\"}}";
        private static final String JSON_INVALID_CHARS_PARTNERSHIP_NAME = "{\"data\":{\"partnership_name\":\"±±±Name test\", \"name_ending\":\"Limited Partnership\",\"partnership_type\":\"LP\"}}";
        private static final String JSON_MISSING_TYPE = "{\"data\":{\"partnership_name\":\"Name test\", \"name_ending\":\"Limited Partnership\"}}";

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_MISSING_PARTNERSHIP_NAME,
                JSON_INVALID_CHARS_PARTNERSHIP_NAME,
                JSON_MISSING_TYPE
        })
        void shouldReturnBadRequest(String body) throws Exception {
            mockMvc.perform(post(PartnershipControllerValidationTest.POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
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
            mocks();

            mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Nested
        class Email {
            @Test
            void shouldReturn200() throws Exception {
                mocks();

                String body = "{\"email\":\"test@email.com\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturnBadRequestErrorIfEmailBadlyFormated() throws Exception {
                String body = "{\"email\":\"test@email.\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
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
                mocks();

                String body = "{\"partnership_name\":\"Correct name size\",\"name_ending\":\"Limited Partnership\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturnBadRequestErrorIfNameSizeIsTooLong() throws Exception {
                String longName = StringUtils.repeat("A", 160);
                String body = "{\"partnership_name\":\"" + longName + "\",\"name_ending\":\"Limited Partnership\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.['errors'].['data.partnershipName']").value("Max length 'partnership name + name ending' is 160 characters"));

            }
        }

        @Nested
        class NameInvalidChars {
            @Test
            void shouldReturnBadRequestErrorIfNameContainsInvalidChars() throws Exception {
                String body = "{\"partnership_name\":\"±±\",\"name_ending\":\"Limited Partnership\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
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

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.jurisdiction").value("Jurisdiction must be valid"));
            }
        }

        @Nested
        class PartnershipNameEnding {
            @Test
            void testUpdatePartnershipWithAnInvalidPartnershipNameEndingShouldReturn400() throws Exception {
                String body = "{\"name_ending\":\"Illegal\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.nameEnding").value("Name ending must be valid"));
            }
        }

        @Nested
        class PartnershipType {
            @Test
            void testUpdatePartnershipWithAnInvalidPartnershipTypeShouldReturn400() throws Exception {
                String body = "{\"partnership_type\":\"Illegal\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.partnershipType").value("Partnership type must be valid"));
            }
        }

        @Nested
        class Addresses {
            // registered_office_address
            private static final String JSON_ROA_POSTCODE_EMPTY = "{\"registered_office_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_ROA_POSTCODE_NOT_CORRECT = "{\"registered_office_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_ROA_POSTCODE_NOT_UK_MAINLAND = "{\"registered_office_address\":{\"postal_code\":\"JE2 3AA\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_ROA_ADDRESS_LINE_1_TOO_SHORT = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

            private static final String JSON_ROA_MISSING_POSTCODE = "{\"registered_office_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_ROA_MISSING_PREMISES = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_ROA_MISSING_ADDRESS_LINE_1 = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_ROA_MISSING_LOCALITY = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
            private static final String JSON_ROA_MISSING_COUNTRY = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

            private static final String JSON_ROA_PREMISES_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"±\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_ROA_ADDRESS_LINE_1_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"±±DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_ROA_ADDRESS_LINE_2_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"±±\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_ROA_ADDRESS_LOCALITY_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"±±STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_ROA_ADDRESS_REGION_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"±±Herefordshire\", \"country\":\"England\"}}";
            private static final String JSON_ROA_ADDRESS_POSTCODE_INVALID_CHARS = "{\"registered_office_address\":{\"postal_code\":\"±±ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"Herefordshire\", \"country\":\"England\"}}";

            // principal place of business
            private static final String JSON_PPOB_POSTCODE_EMPTY = "{\"principal_place_of_business_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_PPOB_POSTCODE_NOT_CORRECT = "{\"principal_place_of_business_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_PPOB_ADDRESS_LINE_1_TOO_SHORT = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

            private static final String JSON_PPOB_MISSING_POSTCODE = "{\"principal_place_of_business_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_PPOB_MISSING_PREMISES = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_PPOB_MISSING_ADDRESS_LINE_1 = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
            private static final String JSON_PPOB_MISSING_LOCALITY = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
            private static final String JSON_PPOB_MISSING_COUNTRY = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

            private static final String JSON_PPOB_PREMISES_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"±\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_PPOB_ADDRESS_LINE_1_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"±±DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_PPOB_ADDRESS_LINE_2_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"±±\",\"locality\":\"STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_PPOB_ADDRESS_LOCALITY_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"±±STOKE-ON-TRENT\", \"country\":\"England\"}}";
            private static final String JSON_PPOB_ADDRESS_REGION_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"±±Herefordshire\", \"country\":\"England\"}}";
            private static final String JSON_PPOB_ADDRESS_POSTCODE_INVALID_CHARS = "{\"principal_place_of_business_address\":{\"postal_code\":\"±±ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\", \"region\":\"Herefordshire\", \"country\":\"England\"}}";

            @Test
            void shouldReturn200() throws Exception {
                mocks();

                String body = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
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
                String body = "{\"registered_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"" + longAddressLine1 + "\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    JSON_ROA_POSTCODE_EMPTY,
                    JSON_ROA_POSTCODE_NOT_CORRECT,
                    JSON_ROA_POSTCODE_NOT_UK_MAINLAND,
                    JSON_ROA_ADDRESS_LINE_1_TOO_SHORT,
                    JSON_PPOB_POSTCODE_EMPTY,
                    JSON_PPOB_POSTCODE_NOT_CORRECT,
                    JSON_PPOB_ADDRESS_LINE_1_TOO_SHORT
            })
            void shouldReturn400IfFieldIncorrect(String body) throws Exception {
                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
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
                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @MethodSource("provideInvalidCharsInputsAndMessages")
            void shouldReturn400IfFieldHasInvalidChars(String body, String fieldName, String expectedErrorMessage) throws Exception {
                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
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
                mocks();

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturn400IfTermIncorrect() throws Exception {
                String body = "{\"term\":\"wrong-term\"}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.term").value("Term must be valid"));
            }
        }

        @Nested
        class SicCodes {
            @Test
            void shouldReturn200IfSicCodesIsCorrect() throws Exception {
                mocks();

                String body = "{\"sic_codes\":[\"12345\"]}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isOk());
            }

            @Test
            void shouldReturn400IfSicCodesIncorrect() throws Exception {
                String body = "{\"sic_codes\":[\"abcde\", \"123A5\", \"123\", \"123456\", \"12345\"]}";

                mockMvc.perform(patch(PartnershipControllerValidationTest.PATCH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .headers(httpHeaders)
                                .requestAttr("transaction", transaction)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("errors.['sicCodes[0]']").value("Sic code must be 5 numeric characters"))
                        .andExpect(jsonPath("errors.['sicCodes[1]']").value("Sic code must be 5 numeric characters"))
                        .andExpect(jsonPath("errors.['sicCodes[2]']").value("Sic code must be 5 numeric characters"))
                        .andExpect(jsonPath("errors.['sicCodes']").value("Sic codes list must contain at least 1 sic code, and no more than 4 sic codes"));
            }
        }
    }

    @Nested
    class ValidatePartnership {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            mocks();

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("true"));
        }

        @Test
        void shouldReturn200AndErrorDetailsIfErrors() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .buildDao();

            limitedPartnershipDao.getData().setTerm(Term.UNKNOWN);

            mocks(limitedPartnershipDao);

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("false"))
                    .andExpect(jsonPath("$.['errors'][0].['location']").value("data.term"))
                    .andExpect(jsonPath("$.['errors'][0].['error']").value("Term must be valid"));
        }

        @Test
        void shouldReturn200AndErrorsIfRegisteredOfficeAddressIsMissing() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .withRegisteredOfficeAddress(null)
                    .buildDao();

            mocks(limitedPartnershipDao);

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("false"))
                    .andExpect(jsonPath("$.['errors']").value(containsInAnyOrder(
                            allOf(hasEntry("location", "data.registeredOfficeAddress"), hasEntry("error", "Registered office address is required")))
                    ));
        }

        @Test
        void shouldReturn200AndErrorsIfPrincipalPlaceOfBusinessAddressIsMissing() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .withPrincipalPlaceOfBusinessAddress(null)
                    .buildDao();

            mocks(limitedPartnershipDao);

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("false"))
                    .andExpect(jsonPath("$.['errors']").value(containsInAnyOrder(
                            allOf(hasEntry("location", "data.principalPlaceOfBusinessAddress"), hasEntry("error", "Principal place of business address is required")))
                    ));
        }

        @Test
        void shouldReturn200AndNoErrorsIfPrincipalPlaceOfBusinessAddressIsMissingInTransitionJourney() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .withPrincipalPlaceOfBusinessAddress(null)
                    .buildDao();

            mocks(limitedPartnershipDao);

            transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("true"));
        }

        @Test
        void shouldReturn200AndNoErrorsIfTermIsMissingInTransitionJourney() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .withTerm(null)
                    .buildDao();

            mocks(limitedPartnershipDao);

            transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("true"));
        }

        @Test
        void shouldReturn200AndNoErrorsIfSicCodeIsMissingInTransitionJourney() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .withSicCodes(null)
                    .buildDao();

            mocks(limitedPartnershipDao);

            transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("true"));
        }

        @Test
        void shouldReturn200AndNoErrorsIfLawfulPurposeStatementCheckedIsMissingInTransitionJourney() throws Exception {
            LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                    .withAddresses()
                    .withLawfulPurposeStatementChecked(false)
                    .buildDao();

            mocks(limitedPartnershipDao);

            transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("true"));
        }

        @Test
        void shouldReturn404IfPartnershipNotFound() throws Exception {
            mockMvc.perform(get(PartnershipControllerValidationTest.VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isNotFound());
        }
    }

    private void mocks(LimitedPartnershipDao limitedPartnershipDao) {
        when(repository.insert((LimitedPartnershipDao) any())).thenReturn(limitedPartnershipDao);
        when(repository.save(any())).thenReturn(limitedPartnershipDao);
        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipDao));
        when(repository.findByTransactionId(TRANSACTION_ID)).thenReturn(List.of(limitedPartnershipDao));

        when(transactionService.doesTransactionHaveALimitedPartnership(any(), any())).thenReturn(true);
    }

    private void mocks() {
        LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
                .withAddresses()
                .buildDao();

        mocks(limitedPartnershipDao);
    }
}
