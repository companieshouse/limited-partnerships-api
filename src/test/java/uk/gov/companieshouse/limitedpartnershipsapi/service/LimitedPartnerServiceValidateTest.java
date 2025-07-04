package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnerServiceValidateTest {

    private static final String LIMITED_PARTNER_ID = LimitedPartnerBuilder.LIMITED_PARTNER_ID;

    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LIMITED_PARTNER_ID
    ).build();

    @Autowired
    private LimitedPartnerService service;

    @MockitoBean
    private LimitedPartnerRepository repository;

    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;

    @Test
    void shouldReturnNoErrorsWhenLimitedPartnerDataIsValid() throws ServiceException {
        // given
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        mocks();

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        // then
        verify(repository).findById(limitedPartnerDao.getId());
        assertEquals(0, results.size());
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerDataIsInvalidAndJavaBeanChecksFail() throws ServiceException {
        // given
        LimitedPartnerDao limitedPartnerDao = createPersonDao();
        limitedPartnerDao.getData().setDateOfBirth(LocalDate.of(3000, 10, 3));
        limitedPartnerDao.getData().setNationality1("INVALID-COUNTRY");

        mocks(limitedPartnerDao);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        // then
        verify(repository).findById(limitedPartnerDao.getId());
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Date of birth must be in the past", "data.dateOfBirth"),
                        tuple("First nationality must be valid", "data.nationality1"),
                        tuple("Contribution currency value is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD),
                        tuple("Contribution currency type is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD),
                        tuple("At least one contribution type must be selected", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD));
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerPersonDataIsInvalidAndCustomChecksFail() throws ServiceException {
        // given
        LimitedPartnerDao limitedPartnerDao = createPersonDao();
        limitedPartnerDao.getData().setDateOfBirth(null);
        limitedPartnerDao.getData().setNationality2(Nationality.EMIRATI.getDescription());
        limitedPartnerDao.getData().setUsualResidentialAddress(null);

        mocks(limitedPartnerDao);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        // then
        verify(repository).findById(limitedPartnerDao.getId());

        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Date of birth is required", LimitedPartnerDataDto.DATE_OF_BIRTH_FIELD),
                        tuple("Second nationality must be different from the first", LimitedPartnerDataDto.NATIONALITY2_FIELD),
                        tuple("Usual residential address is required", LimitedPartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD),
                        tuple("Contribution currency value is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD),
                        tuple("Contribution currency type is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD),
                        tuple("At least one contribution type must be selected", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD));
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerCapitalContributionValuesAreInvalid() throws ServiceException {

        // given
        LimitedPartnerDao limitedPartnerDao = createPersonDao();
        limitedPartnerDao.getData().setContributionCurrencyValue("0.00");

        mocks(limitedPartnerDao);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        // then
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Contribution currency value is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD),
                        tuple("Contribution currency type is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD),
                        tuple("At least one contribution type must be selected", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD)
        );
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = { "LP", "PFLP", "SLP", "SPFLP" })
    void shouldReturnErrorsWhenLimitedPartnerLegalEntityDataIsInvalidAndCustomChecksFail(PartnershipType partnershipType) throws ServiceException {
        var shouldHaveContribution = partnershipType != PartnershipType.PFLP && partnershipType != PartnershipType.SPFLP;
        // given
        LimitedPartnerDao limitedPartnerDao = createLegalEntityDao();
        limitedPartnerDao.getData().setGoverningLaw(null);
        limitedPartnerDao.getData().setRegisteredCompanyNumber(null);
        limitedPartnerDao.getData().setPrincipalOfficeAddress(null);

        mocks(limitedPartnerDao);

        mockLimitedPartnershipsService(partnershipType);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        // then
        verify(repository).findById(limitedPartnerDao.getId());

        List<Tuple> expectedErrors = new ArrayList<>(List.of(
                tuple("Governing Law is required", LimitedPartnerDataDto.GOVERNING_LAW_FIELD),
                tuple("Registered Company Number is required", LimitedPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD),
                tuple("Principal office address is required", LimitedPartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD)
        ));

        if (shouldHaveContribution) {
            expectedErrors.add(tuple("Contribution currency value is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD));
            expectedErrors.add(tuple("Contribution currency type is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD));
            expectedErrors.add(tuple("At least one contribution type must be selected", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD));
        }

        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(expectedErrors.toArray(new Tuple[0]));
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = { "LP", "PFLP", "SLP", "SPFLP" })
    void shouldReturnErrorsWhenLimitedPartnerLegalEntityDataIsInvalidAndJavaBeanAndCustomChecksFail(PartnershipType partnershipType) throws ServiceException {
        var shouldHaveContribution = partnershipType != PartnershipType.PFLP && partnershipType != PartnershipType.SPFLP;

        // given

        mocks();

        LimitedPartnerDao limitedPartnerDao = createLegalEntityDao();
        limitedPartnerDao.getData().setRegisteredCompanyNumber("");
        limitedPartnerDao.getData().setLegalEntityName(null);

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        mockLimitedPartnershipsService(partnershipType);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        List<Tuple> expectedErrors = new ArrayList<>(List.of(
                tuple("Registered company number must be greater than 1", "data.registeredCompanyNumber"),
                tuple("Legal Entity Name is required", LimitedPartnerDataDto.LEGAL_ENTITY_NAME_FIELD)
        ));

        if (shouldHaveContribution) {
            expectedErrors.add(tuple("Contribution currency value is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD));
            expectedErrors.add(tuple("Contribution currency type is required", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD));
            expectedErrors.add(tuple("At least one contribution type must be selected", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD));
        }

        // then
        verify(repository).findById(limitedPartnerDao.getId());
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(expectedErrors.toArray(new Tuple[0]));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenLimitedPartnerIdAndTransactionIdDoNotMatch() {
        // given
        LimitedPartnerDao limitedPartnerDao = createLegalEntityDao();
        limitedPartnerDao.getData().setRegisteredCompanyNumber("");
        limitedPartnerDao.getData().setLegalEntityName(null);

        transaction.getResources().values().forEach(r -> r.setKind("INVALID-KIND"));

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID));
    }

    private static Stream<Arguments> shouldNotAddContributionTestCases() {
        return Stream.of(
                Arguments.of(PartnershipType.PFLP, createLegalEntityDao()),
                Arguments.of(PartnershipType.SPFLP, createLegalEntityDao()),
                Arguments.of(PartnershipType.PFLP, createPersonDao()),
                Arguments.of(PartnershipType.SPFLP, createPersonDao())
        );
    }

    @ParameterizedTest
    @MethodSource("shouldNotAddContributionTestCases")
    void shouldReturnErrorsWhenTryingToAddContributionForPrivateFundTypes(PartnershipType partnershipType, LimitedPartnerDao limitedPartnerDao) throws ServiceException {
        // given

        limitedPartnerDao.getData().setContributionSubTypes(List.of(ContributionSubTypes.MONEY));
        limitedPartnerDao.getData().setContributionCurrencyValue("1000.00");
        limitedPartnerDao.getData().setContributionCurrencyType(Currency.GBP);

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        mocks(limitedPartnerDao);

        mockLimitedPartnershipsService(partnershipType);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        // then
        verify(repository).findById(limitedPartnerDao.getId());
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Private fund partnerships cannot have a contribution", LimitedPartnerDataDto.CONTRIBUTION_SUB_TYPES_FIELD),
                        tuple("Private fund partnerships cannot have a contribution currency value", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_VALUE_FIELD),
                        tuple("Private fund partnerships cannot have a contribution currency type", LimitedPartnerDataDto.CONTRIBUTION_CURRENCY_TYPE_FIELD));
    }

    private static LimitedPartnerDao createPersonDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        dao.setId(LIMITED_PARTNER_ID);
        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setNationality1(Nationality.EMIRATI.getDescription());
        dataDao.setUsualResidentialAddress(createAddressDao());
        dao.setData(dataDao);

        return dao;
    }

    private static LimitedPartnerDao createLegalEntityDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        dao.setId(LIMITED_PARTNER_ID);
        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setLegalEntityRegisterName("Shell Company");
        dataDao.setLegalForm("AA");
        dataDao.setLegalEntityName("Same");
        dataDao.setGoverningLaw("UK");
        dataDao.setLegalEntityRegistrationLocation(Country.UNITED_STATES.getDescription());
        dataDao.setRegisteredCompanyNumber("LP111222");
        dataDao.setPrincipalOfficeAddress(createAddressDao());
        dao.setData(dataDao);

        return dao;
    }

    private static AddressDao createAddressDao() {
        AddressDao dao = new AddressDao();

        dao.setPremises("33");
        dao.setAddressLine1("Acacia Avenue");
        dao.setLocality("Birmingham");
        dao.setCountry("England");
        dao.setPostalCode("BM1 2EH");

        return dao;
    }

    private void mocks(LimitedPartnerDao limitedPartnerDao) throws ServiceException {
        when(repository.insert((LimitedPartnerDao) any())).thenReturn(limitedPartnerDao);
        when(repository.save(any())).thenReturn(limitedPartnerDao);
        when(repository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));
        doNothing().when(repository).deleteById(LIMITED_PARTNER_ID);

        mockLimitedPartnershipsService(PartnershipType.LP);
    }

    private void mocks() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        mocks(limitedPartnerDao);
    }

    private void mockLimitedPartnershipsService(PartnershipType partnershipType) throws ServiceException {
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        limitedPartnershipDto.setData(new DataDto());
        limitedPartnershipDto.getData().setPartnershipType(partnershipType);

        when(limitedPartnershipService.getLimitedPartnership(transaction))
                .thenReturn(limitedPartnershipDto);
    }
}
