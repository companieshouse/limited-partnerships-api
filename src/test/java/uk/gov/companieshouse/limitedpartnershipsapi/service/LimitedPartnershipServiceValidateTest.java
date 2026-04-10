package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.LP;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.PFLP;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.SLP;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType.SPFLP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnershipServiceValidateTest {

    Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private LimitedPartnershipService service;

    @MockitoBean
    private LimitedPartnershipRepository repository;

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnNoErrorsWhenPartnershipDataIsValid(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        setupPartnershipTypeSpecificFields(limitedPartnershipSubmissionDao, type);

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(0, results.size());
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndJavaBeanChecksFail(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        setupPartnershipTypeSpecificFields(limitedPartnershipSubmissionDao, type);

        limitedPartnershipSubmissionDao.getData().setPartnershipName(null);
        limitedPartnershipSubmissionDao.getData().setEmail("invalid-email-address-format");
        limitedPartnershipSubmissionDao.getData().getRegisteredOfficeAddress().setAddressLine1(null);
        limitedPartnershipSubmissionDao.getData().getPrincipalPlaceOfBusinessAddress().setPostalCode("invalid-postal-code-format-and-too-long");
        limitedPartnershipSubmissionDao.getData().getPrincipalPlaceOfBusinessAddress().setAddressLine1(null);
        limitedPartnershipSubmissionDao.getData().setLawfulPurposeStatementChecked(false);
        limitedPartnershipSubmissionDao.getData().setDateOfUpdate(LocalDate.now().plusDays(1));

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(7, results.size());
        checkForError(results, "Limited partnership name must not be null", "data.partnershipName");
        checkForError(results, "must be a well-formed email address", "data.email");
        checkForError(results, "Address line 1 must not be null", "data.registeredOfficeAddress.addressLine1");
        checkForError(results, "Postcode must be less than 15", "data.principalPlaceOfBusinessAddress.postalCode");
        checkForError(results, "Address line 1 must not be null", "data.principalPlaceOfBusinessAddress.addressLine1");
        checkForError(results, "Lawful purpose statement checked is required", "data.lawfulPurposeStatementChecked");
        checkForError(results, "Date of update must not be in the future", "data.dateOfUpdate");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndCustomChecksFail(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        limitedPartnershipSubmissionDao.getData().setEmail(null);
        limitedPartnershipSubmissionDao.getData().setJurisdiction(null);
        limitedPartnershipSubmissionDao.getData().setRegisteredOfficeAddress(null);
        limitedPartnershipSubmissionDao.getData().setPrincipalPlaceOfBusinessAddress(null);
        limitedPartnershipSubmissionDao.getData().setLawfulPurposeStatementChecked(null);
        limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(null);

        var errorMessageAddition = "";
        if (LP.equals(type) || SLP.equals(type)) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        } else {
            limitedPartnershipSubmissionDao.getData().setTerm(Term.BY_AGREEMENT);
            limitedPartnershipSubmissionDao.getData().setSicCodes(List.of("12345", "88222", "12334", "45457"));
            errorMessageAddition = "not ";
        }

        if (SLP.equals(type) || SPFLP.equals(type)) {
            limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(true);
        }

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
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
    void shouldReturnErrorWhen(PartnershipType partnershipType) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(partnershipType);

        var errorMessage = "";
        if (LP.equals(partnershipType) || PFLP.equals(partnershipType)) {
            limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(true);
            errorMessage = "This type of partnership can not have a person with significant control";
        }
        if (SLP.equals(partnershipType) || SPFLP.equals(partnershipType)) {
            limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(null);
            errorMessage = "You must declare whether the partnership will or will not have a person with significant control";
        }

        if (PFLP.equals(partnershipType) || SPFLP.equals(partnershipType)) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        checkForError(results, errorMessage, "data.hasPersonWithSignificantControl");
        assertEquals(1, results.size());
    }

    @ParameterizedTest
    @EnumSource(value = FilingMode.class, names = {"UNKNOWN", "DEFAULT"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldOnlyValidateHasPscWhenFilingModeIsRegistration(FilingMode filingMode) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(SLP);
        transaction.setFilingMode(filingMode.getDescription());
        limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(null);

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        if (FilingMode.REGISTRATION.equals(filingMode)) {
            var errorMessage = "You must declare whether the partnership will or will not have a person with significant control";
            checkForError(results, errorMessage, "data.hasPersonWithSignificantControl");
            assertEquals(1, results.size());
        } else {
            assertEquals(0, results.size());
        }
    }

    @ParameterizedTest
    @EnumSource(value = FilingMode.class, names = {"UNKNOWN", "DEFAULT"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorWhenHasPscNotNullAndIsNotRegistration(FilingMode filingMode) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(SLP);
        transaction.setFilingMode(filingMode.getDescription());
        limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(true);

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        if (!FilingMode.REGISTRATION.equals(filingMode)) {
            var errorMessage = "You can only declare whether the partnership will or will not have a person with significant control during registration";
            checkForError(results, errorMessage, "data.hasPersonWithSignificantControl");
            assertEquals(1, results.size());
        } else {
            assertEquals(0, results.size());
        }
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorsWhenPartnershipDataIsInvalidAndJavaBeanAndCustomChecksFail(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        setupPartnershipTypeSpecificFields(limitedPartnershipSubmissionDao, type);

        limitedPartnershipSubmissionDao.getData().setPartnershipName("");
        limitedPartnershipSubmissionDao.getData().setEmail(null);

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(2, results.size());
        checkForError(results, "Limited partnership name must be greater than 1", "data.partnershipName");
        checkForError(results, "Email is required", "data.email");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorWhenPartnershipNameEndingIsMissingForARegistration(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        setupPartnershipTypeSpecificFields(limitedPartnershipSubmissionDao, type);

        limitedPartnershipSubmissionDao.getData().setNameEnding(null);

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(1, results.size());
        checkForError(results, "Name ending is required", "data.nameEnding");
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnNoErrorsWhenPartnershipDetailsForATransitionAreCorrect(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        setupPartnershipTypeSpecificFields(limitedPartnershipSubmissionDao, type);

        limitedPartnershipSubmissionDao.getData().setNameEnding(null);
        limitedPartnershipSubmissionDao.getData().setPartnershipNumber("LP123456");
        limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(null);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(0, results.size());
    }

    @ParameterizedTest
    @EnumSource(value = PartnershipType.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnErrorWhenCompanyNumberForATransitionIsIncorrect(PartnershipType type) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao(type);
        setupPartnershipTypeSpecificFields(limitedPartnershipSubmissionDao, type);

        limitedPartnershipSubmissionDao.getData().setNameEnding(null);
        limitedPartnershipSubmissionDao.getData().setPartnershipNumber("LX123456");
        limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(null);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
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

    private void setupPartnershipTypeSpecificFields(LimitedPartnershipDao limitedPartnershipSubmissionDao, PartnershipType partnershipType){
        if (partnershipType == PFLP || partnershipType == PartnershipType.SPFLP) {
            limitedPartnershipSubmissionDao.getData().setTerm(null);
            limitedPartnershipSubmissionDao.getData().setSicCodes(null);
        }
        if (PartnershipType.SLP.equals(partnershipType) || PartnershipType.SPFLP.equals(partnershipType)) {
            limitedPartnershipSubmissionDao.getData().setHasPersonWithSignificantControl(false);
        }
    }
}
