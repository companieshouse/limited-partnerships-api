package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;

import java.util.List;
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

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Disabled
class LimitedPartnershipServiceValidateTest {

    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;

    Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private LimitedPartnershipService service;

    @MockitoBean
    private LimitedPartnershipRepository repository;

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnNoErrorsWhenPartnershipDataIsValid(PartnershipType type) throws ServiceException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        if (type == PartnershipType.PFLP || type == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(0, results.size());
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndJavaBeanChecksFail(PartnershipType type) throws ServiceException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        if (type == PartnershipType.PFLP || type == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }
        limitedPartnershipSubmissionDao.getData().setPartnershipName(null);
        limitedPartnershipSubmissionDao.getData().setEmail("invalid-email-address-format");
        limitedPartnershipSubmissionDao.getData().getRegisteredOfficeAddress().setAddressLine1(null);
        limitedPartnershipSubmissionDao.getData().getPrincipalPlaceOfBusinessAddress().setPostalCode("invalid-postal-code-format-and-too-long");
        limitedPartnershipSubmissionDao.getData().getPrincipalPlaceOfBusinessAddress().setAddressLine1(null);
        limitedPartnershipSubmissionDao.getData().setLawfulPurposeStatementChecked(false);

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(6, results.size());
        checkForError(results, "Limited partnership name must not be null", "data.partnershipName");
        checkForError(results, "must be a well-formed email address", "data.email");
        checkForError(results, "Address line 1 must not be null", "data.registeredOfficeAddress.addressLine1");
        checkForError(results, "Postcode must be less than 15", "data.principalPlaceOfBusinessAddress.postalCode");
        checkForError(results, "Address line 1 must not be null", "data.principalPlaceOfBusinessAddress.addressLine1");
        checkForError(results, "Lawful purpose statement checked is required", "data.lawfulPurposeStatementChecked");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndCustomChecksFail(PartnershipType type) throws ServiceException {
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

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

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
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndJavaBeanAndCustomChecksFail(PartnershipType type) throws ServiceException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        if (type == PartnershipType.PFLP || type == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }
        limitedPartnershipSubmissionDao.getData().setPartnershipName("");
        limitedPartnershipSubmissionDao.getData().setEmail(null);

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(2, results.size());
        checkForError(results, "Limited partnership name must be greater than 1", "data.partnershipName");
        checkForError(results, "Email is required", "data.email");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorWhenPartnershipNameEndingIsMissingForARegistration(PartnershipType type) throws ServiceException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        if (type == PartnershipType.PFLP || type == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }
        limitedPartnershipSubmissionDao.getData().setNameEnding(null);

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(1, results.size());
        checkForError(results, "Name ending is required", "data.nameEnding");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnNoErrorsWhenPartnershipDetailsForATransitionAreCorrect(PartnershipType type) throws ServiceException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        if (type == PartnershipType.PFLP || type == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }
        limitedPartnershipSubmissionDao.getData().setNameEnding(null);
        limitedPartnershipSubmissionDao.getData().setPartnershipNumber("LP123456");

        transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(0, results.size());
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorWhenCompanyNumberForATransitionIsIncorrect(PartnershipType type) throws ServiceException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        if (type == PartnershipType.PFLP || type == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }
        limitedPartnershipSubmissionDao.getData().setNameEnding(null);
        limitedPartnershipSubmissionDao.getData().setPartnershipNumber("LX123456");

        transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        verify(repository).findById(limitedPartnershipSubmissionDao.getId());
        assertEquals(1, results.size());
        checkForError(results, "Partnership number must be valid", "data.partnershipNumber");
    }

    private LimitedPartnershipDao createDao(PartnershipType type) {
        LimitedPartnershipDao dao = new LimitedPartnershipBuilder()
                .withAddresses()
                .buildDao();

        DataDao dataDao = dao.getData();
        dataDao.setPartnershipType(type);

        if (LP.equals(type) || SLP.equals(type)) {
            dataDao.setTerm(Term.BY_AGREEMENT);
            dataDao.setSicCodes(List.of("12345"));
        }

        return dao;
    }

    private void checkForError(List<ValidationStatusError> results, String errorMessage, String location) {
        assertThat(results, hasItem(allOf(
                hasProperty("error", is(errorMessage)),
                hasProperty("location", is(location)))));
    }
}
