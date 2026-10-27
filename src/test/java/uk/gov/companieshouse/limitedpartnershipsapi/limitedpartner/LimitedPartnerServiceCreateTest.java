package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.CompanyBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.enums.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.enums.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode.TRANSITION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnerServiceCreateTest {

    private static final String USER_ID = "xbJf0l";
    private static final String LIMITED_PARTNER_ID = LimitedPartnerBuilder.LIMITED_PARTNER_ID;
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    private final Transaction transaction = new TransactionBuilder().withKindAndUri(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LIMITED_PARTNER_ID
    ).build();

    @Autowired
    private LimitedPartnerService service;

    @MockitoBean
    private LimitedPartnerRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private PartnershipService partnershipService;

    @MockitoBean
    private CompanyService companyService;

    @Captor
    private ArgumentCaptor<LimitedPartnerDao> submissionCaptor;

    @Nested
    class CreateLimitedPartnerLegalEntity {
        @ParameterizedTest
        @CsvSource(value = {
                "registration",
                "transition",
                "default"
        })
        void shouldCreateALimitedPartnerLegalEntityPostTransition(String value) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            LimitedPartnerDto dto = new LimitedPartnerBuilder().legalEntityDto();
            LimitedPartnerDao dao = new LimitedPartnerBuilder().legalEntityDao();

            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            if (value.equals(FilingMode.DEFAULT.getDescription())) {
                dto.getData().setDateEffectiveFrom(LocalDate.now());
                transaction.setFilingMode(FilingMode.DEFAULT.getDescription());
                when(companyService.getCompanyProfile(any())).thenReturn(new CompanyBuilder().withSubtype(PartnershipType.SLP.toString()).build());
            } else {
                mockLimitedPartnershipService();
            }

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(FILING_KIND_LIMITED_PARTNER, sentSubmission.getData().getKind());
            assertEquals(LIMITED_PARTNER_ID, submissionId);

            String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), LIMITED_PARTNER_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));

            assertEquals(PartnershipType.SLP, sentSubmission.getData().getPartnershipType());
        }

        @ParameterizedTest
        @EnumSource(value = FilingMode.class, names = {
                "REGISTRATION",
                "TRANSITION"
        })
        void shouldAddCorrectLinksToTransactionResource(FilingMode filingMode) throws Exception {
            createLimitedPartner(filingMode);

            verify(transactionService).updateTransactionWithLinksForResource(
                    eq(REQUEST_ID), eq(transaction), any(), any(), any());

            Map<String, Resource> transactionResources = transaction.getResources();
            assertEquals(1, transactionResources.size());
            assertThat(transactionResources.values())
                    .allSatisfy(resource -> assertThat(resource.getLinks())
                            .hasSize(1)
                            .isNotNull()
                            .containsKeys(LINK_RESOURCE));
        }

        @Test
        void shouldFailCreateALimitedPartnerLegalEntityIfLegalEntityNameIsCorrectAndOthersAreNull() throws ServiceException {
            LimitedPartnerDto dto = new LimitedPartnerBuilder().legalEntityDto();
            var data = dto.getData();
            data.setLegalEntityRegisterName(null);
            data.setLegalForm(null);
            data.setGoverningLaw(null);
            data.setLegalEntityRegistrationLocation(null);
            data.setRegisteredCompanyNumber(null);
            data.setContributionCurrencyValue(null);
            data.setContributionCurrencyType(null);
            data.setContributionSubTypes(null);

            mockLimitedPartnershipService();

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("legal_entity_name"));
            assertEquals("Legal Entity Register Name is required when entered on register is true", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_register_name")).getDefaultMessage());
            assertEquals("Legal Form is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_form")).getDefaultMessage());
            assertEquals("Governing Law is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
            assertEquals("Legal Entity Registration Location is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
            assertEquals("Registered Company Number is required when entered on register is true", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());

            assertEquals("Contribution currency value is required", Objects.requireNonNull(exception.getBindingResult().getFieldError(LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD)).getDefaultMessage());
            assertEquals("Contribution currency type is required", Objects.requireNonNull(exception.getBindingResult().getFieldError(LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD)).getDefaultMessage());
            assertEquals("At least one contribution type must be selected", Objects.requireNonNull(exception.getBindingResult().getFieldError(LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD)).getDefaultMessage());
        }

        @Test
        void shouldFailCreateALimitedPartnerLegalEntityIfLegalFormIsCorrectAndOthersAreNull() throws ServiceException {
            LimitedPartnerDto dto = new LimitedPartnerBuilder().legalEntityDto();
            var data = dto.getData();

            data.setLegalEntityRegisterName(null);
            data.setGoverningLaw(null);
            data.setLegalEntityRegisterName(null);
            data.setLegalEntityRegistrationLocation(null);
            data.setRegisteredCompanyNumber(null);

            mockLimitedPartnershipService();

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("legal_form"));
            assertEquals("Legal Entity Register Name is required when entered on register is true", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_register_name")).getDefaultMessage());
            assertEquals("Governing Law is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
            assertEquals("Legal Entity Registration Location is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
            assertEquals("Registered Company Number is required when entered on register is true", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
        }

        @Test
        void shouldCreateALimitedPartnerLegalEntityForRemoval() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder()
                    .withLimitedPartnerKind(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY.getDescription())
                    .withCeaseDate(LocalDate.now())
                    .withRemoveConfirmationChecked(true)
                    .legalEntityDto();

            LimitedPartnerDao dao = new LimitedPartnerBuilder().legalEntityDao();

            CompanyProfileApi companyProfileApi = Mockito.mock(CompanyProfileApi.class);
            when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
            when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2020, 1, 1));
            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY.getDescription(), sentSubmission.getData().getKind());
            assertEquals(LIMITED_PARTNER_ID, submissionId);

            String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), LIMITED_PARTNER_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldFailToCreateALimitedPartnerLegalEntityForRemovalIfFutureCeaseDate() throws ServiceException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder()
                    .withLimitedPartnerKind(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY.getDescription())
                    .withCeaseDate(LocalDate.now().plusMonths(1))
                    .withRemoveConfirmationChecked(true)
                    .legalEntityDto();

            CompanyProfileApi companyProfileApi = Mockito.mock(CompanyProfileApi.class);
            when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
            when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2020, 1, 1));

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertEquals("Cease date must not be in the future", Objects.requireNonNull(exception.getBindingResult().getFieldError("data.ceaseDate")).getDefaultMessage());
        }

        private void createLimitedPartner(FilingMode filingMode) throws Exception {
            transaction.setFilingMode(filingMode.getDescription());
            LimitedPartnerDto dto = new LimitedPartnerBuilder().legalEntityDto();
            LimitedPartnerDao dao = new LimitedPartnerBuilder().legalEntityDao();

            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            if (TRANSITION.equals(filingMode)) {
                dto.getData().setDateEffectiveFrom(LocalDate.now().minusDays(1));
                CompanyProfileApi companyProfileApi = Mockito.mock(CompanyProfileApi.class);
                when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
            }

            mockLimitedPartnershipService();

            service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);
        }
    }

    @Nested
    class CreateLimitedPartnerPerson {

        @Test
        void shouldCreateALimitedPartnerPerson() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder().personDto();
            dto.getData().setNationality2(Nationality.BRITISH);
            LimitedPartnerDao dao = createLimitedPartnerPersonDao();
            dao.getData().setNationality2("British");

            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(FILING_KIND_LIMITED_PARTNER, sentSubmission.getData().getKind());
            assertEquals(LIMITED_PARTNER_ID, submissionId);

            String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), LIMITED_PARTNER_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldCreateALimitedPartnerPersonForRemoval() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder()
                    .withLimitedPartnerKind(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON.getDescription())
                    .withCeaseDate(LocalDate.now())
                    .withRemoveConfirmationChecked(true)
                    .personDto();
            LimitedPartnerDao dao = createLimitedPartnerPersonDao();

            CompanyProfileApi companyProfileApi = Mockito.mock(CompanyProfileApi.class);
            when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
            when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2020, 1, 1));
            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON.getDescription(), sentSubmission.getData().getKind());
            assertEquals(LIMITED_PARTNER_ID, submissionId);

            String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), LIMITED_PARTNER_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldFailToCreateALimitedPartnerPersonForRemovalIfFutureCeaseDate() throws ServiceException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder()
                    .withLimitedPartnerKind(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON.getDescription())
                    .withCeaseDate(LocalDate.now().plusMonths(1))
                    .withRemoveConfirmationChecked(true)
                    .personDto();

            CompanyProfileApi companyProfileApi = Mockito.mock(CompanyProfileApi.class);
            when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
            when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2020, 1, 1));

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertEquals("Cease date must not be in the future", Objects.requireNonNull(exception.getBindingResult().getFieldError("data.ceaseDate")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateALimitedPartnerPersonIfForenameIsCorrectAndOthersAreNull() throws ServiceException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder().personDto();
            dto.getData().setSurname(null);
            dto.getData().setDateOfBirth(null);
            dto.getData().setNationality1(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("forename"));
            assertEquals("Surname is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("surname")).getDefaultMessage());
            assertEquals("Date of birth is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("date_of_birth")).getDefaultMessage());
            assertEquals("Nationality1 is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality1")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateALimitedPartnerPersonIfSurnameIsCorrectAndOthersAreNull() throws ServiceException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder().personDto();
            dto.getData().setForename(null);
            dto.getData().setDateOfBirth(null);
            dto.getData().setNationality1(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("surname"));
            assertEquals("Forename is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("forename")).getDefaultMessage());
            assertEquals("Date of birth is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("date_of_birth")).getDefaultMessage());
            assertEquals("Nationality1 is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality1")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateALimitedPartnerPersonIfNationality1AndNationality2AreSame() throws ServiceException {
            mocks();

            LimitedPartnerDto dto = new LimitedPartnerBuilder().personDto();
            dto.getData().setNationality1(Nationality.AMERICAN);
            dto.getData().setNationality2(Nationality.AMERICAN);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("nationality1"));
            assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
        }

        private LimitedPartnerDao createLimitedPartnerPersonDao() {
            LimitedPartnerDao dao = new LimitedPartnerDao();

            LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
            dataDao.setForename("John");
            dataDao.setSurname("Doe");
            dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
            dataDao.setNationality1("American");
            dataDao.setContributionCurrencyType(Currency.GBP);
            dataDao.setContributionCurrencyValue("15.00");
            List<ContributionSubTypes> contributionSubtypes = new ArrayList<>();
            contributionSubtypes.add(ContributionSubTypes.MONEY);
            contributionSubtypes.add(ContributionSubTypes.SERVICES_OR_GOODS);
            dataDao.setContributionSubTypes(contributionSubtypes);

            dao.setData(dataDao);
            dao.setId(LIMITED_PARTNER_ID);

            return dao;
        }
    }

    @Test
    void shouldFailCreateALimitedPartnerPersonIfAllFieldsAreNull() throws ServiceException {
        mockLimitedPartnershipService();

        LimitedPartnerDto dto = new LimitedPartnerDto();
        LimitedPartnerDataDto dataDao = new LimitedPartnerDataDto();
        dto.setData(dataDao);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
        );

        assertNull(exception.getBindingResult().getFieldError("forename"));
        assertNull(exception.getBindingResult().getFieldError("surname"));
        assertNull(exception.getBindingResult().getFieldError("legal_entity_register_name"));
        assertNull(exception.getBindingResult().getFieldError("legal_form"));
        assertEquals("Some fields are missing", Objects.requireNonNull(exception.getBindingResult().getFieldError("")).getDefaultMessage());
    }

    @Nested
    class TransitionCapitalContributions {

        @ParameterizedTest
        @CsvSource({
                "LP, true", "LP, false",
                "SLP, true", "SLP, false",
                "PFLP, true", "PFLP, false",
                "SPFLP, true", "SPFLP, false"
        })
        void shouldCreateLimitedPartnerWhenNoContributionsProvided(PartnershipType partnershipType, boolean isLegalEntity) throws Exception {
            LimitedPartnerDto dto = transitionDto(partnershipType, isLegalEntity);
            dto.getData().setContributionCurrencyValue(null);
            dto.getData().setContributionCurrencyType(null);
            dto.getData().setContributionSubTypes(null);

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            assertEquals(LIMITED_PARTNER_ID, submissionId);
        }

        @ParameterizedTest
        @CsvSource({
                "LP, true", "LP, false",
                "SLP, true", "SLP, false",
                "PFLP, true", "PFLP, false",
                "SPFLP, true", "SPFLP, false"
        })
        void shouldCreateLimitedPartnerWhenContributionSubTypesIsEmpty(PartnershipType partnershipType, boolean isLegalEntity) throws Exception {
            LimitedPartnerDto dto = transitionDto(partnershipType, isLegalEntity);
            dto.getData().setContributionCurrencyValue(null);
            dto.getData().setContributionCurrencyType(null);
            dto.getData().setContributionSubTypes(new ArrayList<>());

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            assertEquals(LIMITED_PARTNER_ID, submissionId);
        }

        @ParameterizedTest
        @CsvSource({
                "LP, true", "LP, false",
                "SLP, true", "SLP, false"
        })
        void shouldCreateLimitedPartnerWhenAllContributionsProvided(PartnershipType partnershipType, boolean isLegalEntity) throws Exception {
            LimitedPartnerDto dto = transitionDto(partnershipType, isLegalEntity);

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            assertEquals(LIMITED_PARTNER_ID, submissionId);
        }

        @ParameterizedTest
        @CsvSource({
                "LP, contribution_currency_value, true",
                "LP, contribution_currency_type, true",
                "LP, contribution_sub_types, true",
                "LP, contribution_currency_value, false",
                "LP, contribution_currency_type, false",
                "LP, contribution_sub_types, false",
                "SLP, contribution_currency_value, true",
                "SLP, contribution_currency_type, true",
                "SLP, contribution_sub_types, true",
                "SLP, contribution_currency_value, false",
                "SLP, contribution_currency_type, false",
                "SLP, contribution_sub_types, false"
        })
        void shouldFailWhenOnlySomeContributionsProvided(PartnershipType partnershipType, String providedField, boolean isLegalEntity) throws Exception {
            LimitedPartnerDto dto = transitionDto(partnershipType, isLegalEntity);
            keepOnlyContributionField(dto, providedField);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            var bindingResult = exception.getBindingResult();
            assertNull(bindingResult.getFieldError(providedField));
            assertEquals(2, bindingResult.getFieldErrorCount());
        }

        @ParameterizedTest
        @CsvSource({
                "PFLP, true", "PFLP, false",
                "SPFLP, true", "SPFLP, false"
        })
        void shouldFailWhenPrivateFundPartnershipHasAllContributions(PartnershipType partnershipType, boolean isLegalEntity) throws Exception {
            LimitedPartnerDto dto = transitionDto(partnershipType, isLegalEntity);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            var bindingResult = exception.getBindingResult();
            assertEquals(3, bindingResult.getFieldErrorCount());
            assertEquals("Private fund partnerships cannot have a contribution currency value",
                    Objects.requireNonNull(bindingResult.getFieldError(LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD)).getDefaultMessage());
            assertEquals("Private fund partnerships cannot have a contribution currency type",
                    Objects.requireNonNull(bindingResult.getFieldError(LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD)).getDefaultMessage());
            assertEquals("Private fund partnerships cannot have a contribution",
                    Objects.requireNonNull(bindingResult.getFieldError(LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD)).getDefaultMessage());
        }

        @ParameterizedTest
        @CsvSource({
                "PFLP, contribution_currency_value, true",
                "PFLP, contribution_currency_type, true",
                "PFLP, contribution_sub_types, true",
                "PFLP, contribution_currency_value, false",
                "PFLP, contribution_currency_type, false",
                "PFLP, contribution_sub_types, false",
                "SPFLP, contribution_currency_value, true",
                "SPFLP, contribution_currency_type, true",
                "SPFLP, contribution_sub_types, true",
                "SPFLP, contribution_currency_value, false",
                "SPFLP, contribution_currency_type, false",
                "SPFLP, contribution_sub_types, false"
        })
        void shouldFailWhenPrivateFundPartnershipHasAnyContribution(PartnershipType partnershipType, String providedField, boolean isLegalEntity) throws Exception {
            LimitedPartnerDto dto = transitionDto(partnershipType, isLegalEntity);
            keepOnlyContributionField(dto, providedField);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            var bindingResult = exception.getBindingResult();
            assertEquals(1, bindingResult.getFieldErrorCount());
            assertThat(Objects.requireNonNull(bindingResult.getFieldError(providedField)).getDefaultMessage())
                    .startsWith("Private fund partnerships cannot have a contribution");
        }

        private void keepOnlyContributionField(LimitedPartnerDto dto, String providedField) {
            var data = dto.getData();
            if (!providedField.equals(LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD)) {
                data.setContributionCurrencyValue(null);
            }
            if (!providedField.equals(LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD)) {
                data.setContributionCurrencyType(null);
            }
            if (!providedField.equals(LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD)) {
                data.setContributionSubTypes(null);
            }
        }

        private LimitedPartnerDto transitionDto(PartnershipType partnershipType, boolean isLegalEntity) throws ServiceException {
            transaction.setFilingMode(TRANSITION.getDescription());

            LimitedPartnerDto dto = isLegalEntity ? new LimitedPartnerBuilder().legalEntityDto() : new LimitedPartnerBuilder().personDto();
            dto.getData().setDateEffectiveFrom(LocalDate.now().minusDays(1));
            LimitedPartnerDao dao = isLegalEntity ? new LimitedPartnerBuilder().legalEntityDao() : new LimitedPartnerBuilder().personDao();

            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);
            when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(Mockito.mock(CompanyProfileApi.class));
            mockLimitedPartnershipService(partnershipType);

            return dto;
        }
    }

    private void mocks(LimitedPartnerDao limitedPartnerDao) throws ServiceException {
        when(repository.insert((LimitedPartnerDao) any())).thenReturn(limitedPartnerDao);
        when(repository.save(any())).thenReturn(limitedPartnerDao);
        when(repository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));
        doNothing().when(repository).deleteById(LIMITED_PARTNER_ID);

        mockLimitedPartnershipService();
    }

    private void mocks() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        mocks(limitedPartnerDao);
    }

    private void mockLimitedPartnershipService() throws ServiceException {
        mockLimitedPartnershipService(PartnershipType.SLP);
    }

    private void mockLimitedPartnershipService(PartnershipType partnershipType) throws ServiceException {
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
        partnershipDto.getData().setPartnershipType(partnershipType);

        when(partnershipService.getLimitedPartnership(transaction)).thenReturn(partnershipDto);
    }
}
