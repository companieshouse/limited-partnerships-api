package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class GeneralPartnerServiceValidateTest {

    private static final String GENERAL_PARTNER_ID = "abc-123";
    private static final String TRANSACTION_ID = "txn-456";

    @Autowired
    private GeneralPartnerService service;

    @MockitoBean
    private GeneralPartnerRepository repository;


    @Test
    void shouldReturnNoErrorsWhenGeneralPartnerDataIsValid() throws ServiceException {
        // given
        GeneralPartnerDao generalPartnerDao = createPersonDao();

        Transaction transaction = buildTransaction();

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertEquals(0, results.size());
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerDataIsInvalidAndJavaBeanChecksFail() throws ServiceException {
        // given
        GeneralPartnerDao generalPartnerDao = createPersonDao();
        generalPartnerDao.getData().setDateOfBirth(LocalDate.of(3000, 10, 3));
        generalPartnerDao.getData().setNationality1("INVALID-COUNTRY");

        Transaction transaction = buildTransaction();

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertEquals(2, results.size());
        checkForError(results, "Date of birth must be in the past", "data.dateOfBirth");
        checkForError(results, "First nationality must be valid", "data.nationality1");
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerPersonDataIsInvalidAndCustomChecksFail() throws ServiceException {
        // given
        GeneralPartnerDao generalPartnerDao = createPersonDao();
        generalPartnerDao.getData().setDateOfBirth(null);
        generalPartnerDao.getData().setNationality2(Nationality.EMIRATI.getDescription());
        generalPartnerDao.getData().setNotDisqualifiedStatementChecked(false);
        generalPartnerDao.getData().setUsualResidentialAddress(null);
        generalPartnerDao.getData().setServiceAddress(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertEquals(5, results.size());
        checkForError(results, "Date of birth is required", GeneralPartnerDataDto.DATE_OF_BIRTH_FIELD);
        checkForError(results, "Second nationality must be different from the first", GeneralPartnerDataDto.NATIONALITY2_FIELD);
        checkForError(results, "Not Disqualified Statement must be checked", GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD);
        checkForError(results, "Usual residential address is required", GeneralPartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD);
        checkForError(results, "Service address is required", GeneralPartnerDataDto.SERVICE_ADDRESS_FIELD);
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerLegalEntityDataIsInvalidAndCustomChecksFail() throws ServiceException {
        // given
        GeneralPartnerDao generalPartnerDao = createLegalEntityDao();
        generalPartnerDao.getData().setGoverningLaw(null);
        generalPartnerDao.getData().setLegalPersonalityStatementChecked(false);
        generalPartnerDao.getData().setRegisteredCompanyNumber(null);
        generalPartnerDao.getData().setPrincipalOfficeAddress(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertEquals(4, results.size());
        checkForError(results, "Governing Law is required", GeneralPartnerDataDto.GOVERNING_LAW_FIELD);
        checkForError(results, "Legal Personality Statement must be checked", GeneralPartnerDataDto.LEGAL_PERSONALITY_STATEMENT_CHECKED_FIELD);
        checkForError(results, "Registered Company Number is required", GeneralPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD);
        checkForError(results, "Principal office address is required", GeneralPartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD);
    }

    @Test
    void shouldReturnErrorsWhenGeneralPartnerLegalEntityDataIsInvalidAndJavaBeanAndCustomChecksFail() throws ServiceException {
        // given
        GeneralPartnerDao generalPartnerDao = createLegalEntityDao();
        generalPartnerDao.getData().setRegisteredCompanyNumber("");
        generalPartnerDao.getData().setLegalEntityName(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // when
        List<ValidationStatusError> results = service.validateGeneralPartner(transaction, GENERAL_PARTNER_ID);

        // then
        verify(repository).findById(generalPartnerDao.getId());
        assertEquals(2, results.size());
        checkForError(results, "Registered company number must be greater than 1", "data.registeredCompanyNumber");
        checkForError(results, "Legal Entity Name is required", GeneralPartnerDataDto.LEGAL_ENTITY_NAME_FIELD);
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_GENERAL_PARTNER);
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/txn-456/limited-partnership/general-partner/abc-123");
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/general-partner/%s", TRANSACTION_ID, GENERAL_PARTNER_ID), resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    private GeneralPartnerDao createPersonDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        dao.setId(GENERAL_PARTNER_ID);
        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setNationality1(Nationality.EMIRATI.getDescription());
        dataDao.setNotDisqualifiedStatementChecked(true);
        dataDao.setUsualResidentialAddress(createAddressDao());
        dataDao.setServiceAddress(createAddressDao());
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
        dataDao.setLegalPersonalityStatementChecked(true);
        dataDao.setPrincipalOfficeAddress(createAddressDao());
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

    private void checkForError(List<ValidationStatusError> results, String errorMessage, String location) {
        assertThat(results, hasItem(allOf(
                hasProperty("error", is(errorMessage)),
                hasProperty("location", is(location)))));
    }
}
