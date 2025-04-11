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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnerServiceValidateTest {

    private static final String LIMITED_PARTNER_ID = "abc-123";
    private static final String TRANSACTION_ID = "txn-456";

    @Autowired
    private LimitedPartnerService service;

    @MockitoBean
    private LimitedPartnerRepository repository;


    @Test
    void shouldReturnNoErrorsWhenLimitedPartnerDataIsValid() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = createPersonDao();

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        verify(repository).findById(limitedPartnerDao.getId());
        assertEquals(0, results.size());
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerDataIsInvalidAndJavaBeanChecksFail() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = createPersonDao();
        limitedPartnerDao.getData().setDateOfBirth(LocalDate.of(3000, 10, 3));
        limitedPartnerDao.getData().setNationality1("INVALID-COUNTRY");

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        verify(repository).findById(limitedPartnerDao.getId());
        assertEquals(2, results.size());
        checkForError(results, "Date of birth must be in the past", "data.dateOfBirth");
        checkForError(results, "First nationality must be valid", "data.nationality1");
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerPersonDataIsInvalidAndCustomChecksFail() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = createPersonDao();
        limitedPartnerDao.getData().setDateOfBirth(null);
        limitedPartnerDao.getData().setNationality2(Nationality.EMIRATI.getDescription());
        limitedPartnerDao.getData().setUsualResidentialAddress(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        verify(repository).findById(limitedPartnerDao.getId());
        assertEquals(3, results.size());
        checkForError(results, "Date of birth is required", LimitedPartnerDataDto.DATE_OF_BIRTH_FIELD);
        checkForError(results, "Second nationality must be different from the first", LimitedPartnerDataDto.NATIONALITY2_FIELD);
        checkForError(results, "Usual residential address is required", LimitedPartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD);
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerLegalEntityDataIsInvalidAndCustomChecksFail() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = createLegalEntityDao();
        limitedPartnerDao.getData().setGoverningLaw(null);
        limitedPartnerDao.getData().setRegisteredCompanyNumber(null);
        limitedPartnerDao.getData().setPrincipalOfficeAddress(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        verify(repository).findById(limitedPartnerDao.getId());
        assertEquals(3, results.size());
        checkForError(results, "Governing Law is required", LimitedPartnerDataDto.GOVERNING_LAW_FIELD);
        checkForError(results, "Registered Company Number is required", LimitedPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD);
        checkForError(results, "Principal office address is required", LimitedPartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD);
    }

    @Test
    void shouldReturnErrorsWhenLimitedPartnerLegalEntityDataIsInvalidAndJavaBeanAndCustomChecksFail() throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = createLegalEntityDao();
        limitedPartnerDao.getData().setRegisteredCompanyNumber("");
        limitedPartnerDao.getData().setLegalEntityName(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        List<ValidationStatusError> results = service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID);

        verify(repository).findById(limitedPartnerDao.getId());
        assertEquals(2, results.size());
        checkForError(results, "Registered company number must be greater than 1", "data.registeredCompanyNumber");
        checkForError(results, "Legal Entity Name is required", LimitedPartnerDataDto.LEGAL_ENTITY_NAME_FIELD);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenLimitedPartnerIdAndTransactionIdDoNotMatch() {
        LimitedPartnerDao limitedPartnerDao = createLegalEntityDao();
        limitedPartnerDao.getData().setRegisteredCompanyNumber("");
        limitedPartnerDao.getData().setLegalEntityName(null);

        Transaction transaction = buildTransaction();
        transaction.getResources().values().stream().forEach(r -> r.setKind("INVALID-KIND"));

        when(repository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        assertThrows(ResourceNotFoundException.class, () -> service.validateLimitedPartner(transaction, LIMITED_PARTNER_ID));
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNER);
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/txn-456/limited-partnership/limited-partner/abc-123");
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/limited-partner/%s", TRANSACTION_ID, LIMITED_PARTNER_ID), resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    private LimitedPartnerDao createPersonDao() {
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

    private LimitedPartnerDao createLegalEntityDao() {
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
