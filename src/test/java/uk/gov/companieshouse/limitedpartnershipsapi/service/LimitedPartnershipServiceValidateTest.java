package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;

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
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.LP;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.SLP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnershipServiceValidateTest {

    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "txn-456";

    @Autowired
    private LimitedPartnershipService service;

    @MockitoBean
    private LimitedPartnershipRepository repository;


    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnNoErrorsWhenPartnershipDataIsValid(PartnershipType type) throws ResourceNotFoundException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(0, results.size());
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndJavaBeanChecksFail(PartnershipType type) throws ResourceNotFoundException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        limitedPartnershipSubmissionDao.getData().setPartnershipName(null);
        limitedPartnershipSubmissionDao.getData().setEmail("invalid-email-address-format");
        limitedPartnershipSubmissionDao.getData().getRegisteredOfficeAddress().setAddressLine1(null);
        limitedPartnershipSubmissionDao.getData().getPrincipalPlaceOfBusinessAddress().setPostalCode("invalid-postal-code-format-and-too-long");
        limitedPartnershipSubmissionDao.getData().getPrincipalPlaceOfBusinessAddress().setAddressLine1(null);
        limitedPartnershipSubmissionDao.getData().setLawfulPurposeStatementChecked(false);

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(6, results.size());
        checkForError(results, "Limited partnership name must not be null", "data.partnershipName");
        checkForError(results, "must be a well-formed email address", "data.email");
        checkForError(results, "Address line 1 must not be null", "data.registeredOfficeAddress.addressLine1");
        checkForError(results, "Postcode must be less than 15", "data.principalPlaceOfBusinessAddress.postalCode");
        checkForError(results, "Address line 1 must not be null", "data.principalPlaceOfBusinessAddress.addressLine1");
        checkForError(results, "Lawful purpose statement must be checked", "data.lawfulPurposeStatementChecked");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndCustomChecksFail(PartnershipType type) throws ResourceNotFoundException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        limitedPartnershipSubmissionDao.getData().setEmail(null);
        limitedPartnershipSubmissionDao.getData().setJurisdiction(null);
        limitedPartnershipSubmissionDao.getData().setRegisteredOfficeAddress(null);
        limitedPartnershipSubmissionDao.getData().setPrincipalPlaceOfBusinessAddress(null);
        limitedPartnershipSubmissionDao.getData().setLawfulPurposeStatementChecked(null);

        var errorMessageAddition = "";
        if (LP.equals(type) || SLP.equals(type)) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        } else {
            limitedPartnershipSubmissionDao.getData().setTerm(Term.BY_AGREEMENT);
            limitedPartnershipSubmissionDao.getData().setSicCodes(List.of("12345", "88222", "12334", "45457"));
            errorMessageAddition = "not ";
        }

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(7, results.size());
        checkForError(results, "Email is required", "data.email");
        checkForError(results, "Jurisdiction is required", "data.jurisdiction");
        checkForError(results, "Registered office address is required", "data.registeredOfficeAddress");
        checkForError(results, "Principal place of business address is required", "data.principalPlaceOfBusinessAddress");
        checkForError(results, "Term is " + errorMessageAddition + "required", "data.term");
        checkForError(results, "SIC codes are " + errorMessageAddition + "required", "data.sicCodes");
        checkForError(results, "Lawful purpose statement checked is required", "data.lawfulPurposeStatementChecked");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndJavaBeanAndCustomChecksFail(PartnershipType type) throws ResourceNotFoundException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        limitedPartnershipSubmissionDao.getData().setPartnershipName("");
        limitedPartnershipSubmissionDao.getData().setEmail(null);

        Transaction transaction = buildTransaction();

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(2, results.size());
        checkForError(results, "Limited partnership name must be greater than 1", "data.partnershipName");
        checkForError(results, "Email is required", "data.email");
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/txn-456/limited-partnership/partnership/abc-123");
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    private LimitedPartnershipDao createDao(PartnershipType type) {
        LimitedPartnershipDao dao = new LimitedPartnershipDao();

        dao.setId(SUBMISSION_ID);
        DataDao dataDao = new DataDao();
        dataDao.setPartnershipType(type);

        if (LP.equals(type) || SLP.equals(type)) {
            dataDao.setTerm(Term.BY_AGREEMENT);
            dataDao.setSicCodes(List.of("12345"));
        }

        dataDao.setPartnershipName("Asset Adders");
        dataDao.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP.getDescription());
        dataDao.setEmail("some@where.com");
        dataDao.setJurisdiction(Jurisdiction.ENGLAND_AND_WALES.getApiKey());
        dataDao.setRegisteredOfficeAddress(createAddressDao());
        dataDao.setPrincipalPlaceOfBusinessAddress(createAddressDao());
        dataDao.setLawfulPurposeStatementChecked(true);
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
