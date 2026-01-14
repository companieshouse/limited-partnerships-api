package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GeneralPartnerServiceValidateTest {

    private static final String GENERAL_PARTNER_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_GENERAL_PARTNER,
            URL_GET_GENERAL_PARTNER,
            GENERAL_PARTNER_ID
    ).build();

    @Autowired
    private GeneralPartnerService service;

    @MockitoBean
    private GeneralPartnerRepository repository;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CompanyProfileApi companyProfileApi;

    @BeforeEach
    void setup() throws ServiceException {
        when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
    }

    @Test
    void shouldReturnNoErrorsWhenGeneralPartnerDataIsValid() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();
        generalPartnerDao.setTransactionId(TRANSACTION_ID);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertEquals(0, results.size());
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerDataIsInvalidAndJavaBeanChecksFail() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();
        generalPartnerDao.getData().setDateOfBirth(LocalDate.of(3000, 10, 3));
        generalPartnerDao.getData().setNationality1("INVALID-COUNTRY");

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Date of birth must be in the past", "data.dateOfBirth"),
                        tuple("First nationality must be valid", "data.nationality1"));
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerPersonDataIsInvalidAndCustomChecksFail() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = createPersonDao();
        generalPartnerDao.getData().setDateOfBirth(null);
        generalPartnerDao.getData().setNationality2(Nationality.EMIRATI.getDescription());
        generalPartnerDao.getData().setNotDisqualifiedStatementChecked(false);
        generalPartnerDao.getData().setUsualResidentialAddress(null);
        generalPartnerDao.getData().setServiceAddress(null);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());

        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Date of birth is required", GeneralPartnerDataDto.DATE_OF_BIRTH_FIELD),
                        tuple("Second nationality must be different from the first", GeneralPartnerDataDto.NATIONALITY2_FIELD),
                        tuple("Not Disqualified Statement must be checked", GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD),
                        tuple("Usual residential address is required", GeneralPartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD),
                        tuple("Service address is required", GeneralPartnerDataDto.SERVICE_ADDRESS_FIELD));
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerLegalEntityDataIsInvalidAndCustomChecksFail() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = createLegalEntityDao();
        generalPartnerDao.getData().setGoverningLaw(null);
        generalPartnerDao.getData().setRegisteredCompanyNumber(null);
        generalPartnerDao.getData().setPrincipalOfficeAddress(null);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Governing Law is required", GeneralPartnerDataDto.GOVERNING_LAW_FIELD),
                        tuple("Registered Company Number is required", GeneralPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD),
                        tuple("Principal office address is required", GeneralPartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerLegalEntityDataIsInvalidAndJavaBeanAndCustomChecksFail() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = createLegalEntityDao();
        generalPartnerDao.getData().setRegisteredCompanyNumber("");
        generalPartnerDao.getData().setLegalEntityRegisterName(null);
        generalPartnerDao.getData().setKind(FILING_KIND_GENERAL_PARTNER);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertThat(results)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Registered company number must be greater than 1", "data.registeredCompanyNumber"),
                        tuple("Legal Entity Register Name is required", GeneralPartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGeneralPartnerIdAndTransactionIdDoNotMatch() {
        // given
        GeneralPartnerDao generalPartnerDao = createLegalEntityDao();
        generalPartnerDao.getData().setRegisteredCompanyNumber("");
        generalPartnerDao.getData().setLegalEntityName(null);

        transaction.getResources().values().forEach(r -> r.setKind("INVALID-KIND"));

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID));
    }

    @Test
    void shouldReturnErrorWhenNotDisqualifiedStatementCheckedIsNull() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = createPersonDao();
        generalPartnerDao.getData().setNotDisqualifiedStatementChecked(null);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> errors = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertThat(errors)
                .extracting(ValidationStatusError::getError, ValidationStatusError::getLocation)
                .containsExactlyInAnyOrder(
                        tuple("Not Disqualified Statement must be checked", GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD));
    }

    @Test
    void shouldNotReturnErrorWhenNotDisqualifiedStatementCheckedIsNullForATransitionFiling() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        GeneralPartnerDao generalPartnerDao = createPersonDao();
        generalPartnerDao.getData().setNotDisqualifiedStatementChecked(null);
        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2022, 1, 3));

        // when
        List<ValidationStatusError> errors = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertThat(errors).isEmpty();
    }

    private GeneralPartnerDao createPersonDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        dao.setId(GENERAL_PARTNER_ID);
        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setDateEffectiveFrom(LocalDate.of(2023, 10, 3));
        dataDao.setNationality1(Nationality.EMIRATI.getDescription());
        dataDao.setNotDisqualifiedStatementChecked(true);
        dataDao.setUsualResidentialAddress(createAddressDao());
        dataDao.setServiceAddress(createAddressDao());
        dataDao.setKind(FILING_KIND_GENERAL_PARTNER);
        dao.setData(dataDao);

        return dao;
    }

    private GeneralPartnerDao createLegalEntityDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        dao.setId(GENERAL_PARTNER_ID);
        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityRegisterName("Shell Company");
        dataDao.setLegalForm("AA");
        dataDao.setLegalEntityName("Same");
        dataDao.setGoverningLaw("UK");
        dataDao.setLegalEntityRegistrationLocation(Country.UNITED_STATES.getDescription());
        dataDao.setRegisteredCompanyNumber("LP111222");
        dataDao.setPrincipalOfficeAddress(createAddressDao());
        dataDao.setKind(FILING_KIND_GENERAL_PARTNER);
        dao.setData(dataDao);

        return dao;
    }

    private AddressDao createAddressDao() {
        AddressDao dao = new AddressDao();

        dao.setPremises("33");
        dao.setAddressLine1("Acacia Avenue");
        dao.setLocality("Birmingham");
        dao.setCountry("England");
        dao.setPostalCode("BM1 2EH");

        return dao;
    }
}
