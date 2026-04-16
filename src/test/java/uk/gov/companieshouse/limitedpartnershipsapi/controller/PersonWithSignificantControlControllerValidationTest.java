package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PersonWithSignificantControlMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.PersonWithSignificantControlService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.IndividualPersonValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.OtherRegistrablePersonValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.PersonWithSignificantControlValidatorFactory;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.RelevantLegalEntityValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.UnknownTypeValidator;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ContextConfiguration(classes = {
        PersonWithSignificantControlController.class,
        PersonWithSignificantControlService.class,
        ValidationStatus.class,
        PersonWithSignificantControlMapperImpl.class,
        GlobalExceptionHandler.class,
        PersonWithSignificantControlValidatorFactory.class,
        IndividualPersonValidator.class,
        RelevantLegalEntityValidator.class,
        OtherRegistrablePersonValidator.class,
        UnknownTypeValidator.class}
)
@WebMvcTest(controllers = {PersonWithSignificantControlController.class})
class PersonWithSignificantControlControllerValidationTest {

    private static final String PERSON_WITH_SIGNIFICANT_CONTROL_ID = "1234jdhs636";
    private static final String TRANSACTION_ID = "8676782367823";
    private static final String TOO_MANY_CHARS = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";


    private static final String BASE_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/person-with-significant-control";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().withKindAndUri(
            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
            URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL,
            PERSON_WITH_SIGNIFICANT_CONTROL_ID
    ).build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonWithSignificantControlRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CostsService costsService;

    // RLE
    private static final String JSON_CORRECT_RLE = """
            {
                "data": {
                    "kind": "limited-partnership#person-with-significant-control",
                    "type": "RELEVANT_LEGAL_ENTITY",
                    "legal_entity_name": "asasd",
                    "legal_form": "dsfs",
                    "governing_law": "sadsad",
                    "legal_entity_register_name": "REG NAME",
                    "legal_entity_registration_location": "Wales",
                    "registered_company_number": "12345"
                }
            }""";

    private static final String JSON_CORRECT_MANDATORY_ONLY_RLE = """
            {
                "data": {
                    "kind": "limited-partnership#person-with-significant-control",
                    "type": "RELEVANT_LEGAL_ENTITY",
                    "legal_entity_name": "asasd",
                    "legal_form": "dsfs",
                    "governing_law": "sadsad"
                }
            }""";

    private static final String JSON_NAME_IS_REQUIRED_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"\", \"legal_form\": \"dsfs\", \"governing_law\": \"sadsad\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_LEGAL_FORM_IS_REQUIRED_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"Smiths\", \"legal_form\": \"\", \"governing_law\": \"sadsad\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_GOVERNING_LAW_IS_REQUIRED_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"Smiths\", \"legal_form\": \"ddds\", \"governing_law\": \"\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_NAME_INVALID_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"§§\", \"legal_form\": \"dsfs\", \"governing_law\": \"sadsad\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_LEGAL_FORM_INVALID_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaa\", \"legal_form\": \"§§§\", \"governing_law\": \"sadsad\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_GOVERNING_LAW_INVALID_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaa\", \"legal_form\": \"aaa\", \"governing_law\": \"§§§\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_LEGAL_ENTITY_REGISTER_NAME_INVALID_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaa\", \"legal_form\": \"aaa\", \"governing_law\": \"aaa\", \"legal_entity_register_name\": \"§§§\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_REGISTERED_COMPANY_NUMBER_INVALID_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaa\", \"legal_form\": \"aaa\", \"governing_law\": \"aaa\", \"legal_entity_register_name\": \"aaa\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"§§\" }}";
    private static final String JSON_INVALID_LEGAL_ENTITY_REGISTRATION_LOCATION_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaa\", \"legal_form\": \"aaa\", \"governing_law\": \"aaa\", \"legal_entity_register_name\": \"aaa\", \"legal_entity_registration_location\": \"BOB\", \"registered_company_number\": \"aa\" }}";
    private static final String JSON_NAME_IS_ABOVE_MAX_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"" + TOO_MANY_CHARS + "\", \"legal_form\": \"dsfs\", \"governing_law\": \"sadsad\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_LEGAL_FORM_IS_ABOVE_MAX_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaaa\", \"legal_form\": \"" + TOO_MANY_CHARS + "\", \"governing_law\": \"sadsad\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_GOVERNING_LAW_IS_ABOVE_MAX_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaaa\", \"legal_form\": \"aaa\", \"governing_law\": \"" + TOO_MANY_CHARS + "\", \"legal_entity_register_name\": \"REG NAME\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_LEGAL_ENTITY_REGISTER_NAME_IS_ABOVE_MAX_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaaa\", \"legal_form\": \"aaa\", \"governing_law\": \"ww\", \"legal_entity_register_name\": \"" + TOO_MANY_CHARS + "\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"12345\" }}";
    private static final String JSON_REGISTERED_COMPANY_NUMBER_IS_ABOVE_MAX_CHARS_RLE = "{ \"data\": { \"kind\": \"limited-partnership#person-with-significant-control\", \"type\": \"RELEVANT_LEGAL_ENTITY\", \"legal_entity_name\": \"aaaa\", \"legal_form\": \"aaa\", \"governing_law\": \"ww\", \"legal_entity_register_name\": \"sss\", \"legal_entity_registration_location\": \"Wales\", \"registered_company_number\": \"" + TOO_MANY_CHARS + "\" }}";

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }


    @ParameterizedTest
    @ValueSource(strings = {JSON_CORRECT_RLE, JSON_CORRECT_MANDATORY_ONLY_RLE})
    void shouldReturn201_RLE(String jsonPayload) throws Exception {
        mocks();
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(jsonPayload))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @CsvSource(value = {
            JSON_NAME_IS_REQUIRED_RLE + "$ data.legalEntityName $ Name is required",
            JSON_LEGAL_FORM_IS_REQUIRED_RLE + "$ data.legalForm $ Legal form is required",
            JSON_GOVERNING_LAW_IS_REQUIRED_RLE + "$ data.governingLaw $ Governing law is required",
            JSON_NAME_INVALID_CHARS_RLE + "$ data.legalEntityName $ Name " + INVALID_CHARACTERS_MESSAGE,
            JSON_LEGAL_FORM_INVALID_CHARS_RLE + "$ data.legalForm $ Legal form " + INVALID_CHARACTERS_MESSAGE,
            JSON_GOVERNING_LAW_INVALID_CHARS_RLE + "$ data.governingLaw $ Governing law " + INVALID_CHARACTERS_MESSAGE,
            JSON_LEGAL_ENTITY_REGISTER_NAME_INVALID_CHARS_RLE + "$ data.legalEntityRegisterName $ Legal entity register name " + INVALID_CHARACTERS_MESSAGE,
            JSON_REGISTERED_COMPANY_NUMBER_INVALID_CHARS_RLE + "$ data.registeredCompanyNumber $ Registered company number " + INVALID_CHARACTERS_MESSAGE,
            JSON_INVALID_LEGAL_ENTITY_REGISTRATION_LOCATION_RLE + "$ data.legalEntityRegistrationLocation $ Legal entity registration location must be valid",
            JSON_NAME_IS_ABOVE_MAX_CHARS_RLE + "$ data.legalEntityName $ Name " + "must be less than 160",
            JSON_LEGAL_FORM_IS_ABOVE_MAX_CHARS_RLE + "$ data.legalForm $ Legal form " + "must be less than 160",
            JSON_GOVERNING_LAW_IS_ABOVE_MAX_CHARS_RLE + "$ data.governingLaw $ Governing law " + "must be less than 160",
            JSON_LEGAL_ENTITY_REGISTER_NAME_IS_ABOVE_MAX_CHARS_RLE + "$ data.legalEntityRegisterName $ Legal entity register name " + "must be less than 160",
            JSON_REGISTERED_COMPANY_NUMBER_IS_ABOVE_MAX_CHARS_RLE + "$ data.registeredCompanyNumber $ Registered company number " + "must be less than 160"
    }, delimiter = '$')
    void shouldReturn400_RLE(String body, String field, String errorMessage) throws Exception {
        mocks();
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
    }

    private void mocks(PersonWithSignificantControlDao personWithSignificantControlDao) {
        when(repository.insert((PersonWithSignificantControlDao) any())).thenReturn(personWithSignificantControlDao);
        when(repository.save(any())).thenReturn(personWithSignificantControlDao);
        when(repository.findById(PERSON_WITH_SIGNIFICANT_CONTROL_ID)).thenReturn(Optional.of(personWithSignificantControlDao));

        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);
    }

    private void mocks() {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().build();
        mocks(personWithSignificantControlDao);
    }
}
