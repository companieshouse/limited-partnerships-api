package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnerServiceCreateTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String TRANSACTION_ID = "txn-456";

    @Autowired
    private LimitedPartnerService service;

    @MockitoBean
    private LimitedPartnerRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnerDao> submissionCaptor;

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNER);
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/txn-456/limited-partnership/limited-partner/abc-123");
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    @Nested
    class CreateLimitedPartnerLegalEntity {
        @Test
        void shouldCreateALimitedPartnerLegalEntity() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerLegalEntityDto();
            LimitedPartnerDao dao = createLimitedPartnerLegalEntityDao();

            when(repository.insert((LimitedPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            String submissionId = service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(FILING_KIND_LIMITED_PARTNER, sentSubmission.getData().getKind());
            assertEquals(SUBMISSION_ID, submissionId);

            String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), SUBMISSION_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldFailCreateALimitedPartnerLegalEntityIfLegalEntityRegisterNameIsCorrectAndOthersAreNull() {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerLegalEntityDto();
            var data = dto.getData();
            data.setLegalEntityName(null);
            data.setLegalForm(null);
            data.setGoverningLaw(null);
            data.setLegalEntityRegistrationLocation(null);
            data.setRegisteredCompanyNumber(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("legal_entity_register_name"));
            assertEquals("Legal Entity Name is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_name")).getDefaultMessage());
            assertEquals("Legal Form is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_form")).getDefaultMessage());
            assertEquals("Governing Law is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
            assertEquals("Legal Entity Registration Location is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
            assertEquals("Registered Company Number is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateALimitedPartnerLegalEntityIfLegalFormIsCorrectAndOthersAreNull() {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerLegalEntityDto();
            var data = dto.getData();

            data.setLegalEntityName(null);
            data.setGoverningLaw(null);
            data.setLegalEntityRegisterName(null);
            data.setLegalEntityRegistrationLocation(null);
            data.setRegisteredCompanyNumber(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("legal_form"));
            assertEquals("Legal Entity Name is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_name")).getDefaultMessage());
            assertEquals("Governing Law is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
            assertEquals("Legal Entity Register Name is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_register_name")).getDefaultMessage());
            assertEquals("Legal Entity Registration Location is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
            assertEquals("Registered Company Number is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
        }

        private LimitedPartnerDto createLimitedPartnerLegalEntityDto() {
            LimitedPartnerDto dto = new LimitedPartnerDto();

            LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
            dataDto.setLegalEntityName("Legal Entity Name");
            dataDto.setLegalForm("Form");
            dataDto.setGoverningLaw("Act of law");
            dataDto.setLegalEntityRegisterName("Register of United States");
            dataDto.setLegalEntityRegistrationLocation(Country.UNITED_STATES);
            dataDto.setContributionCurrencyType(Currency.GBP);
            dataDto.setRegisteredCompanyNumber("12345678");

            dto.setData(dataDto);
            return dto;
        }

        private LimitedPartnerDao createLimitedPartnerLegalEntityDao() {
            LimitedPartnerDao dao = new LimitedPartnerDao();

            LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
            dataDao.setLegalEntityName("My company ltd");
            dataDao.setLegalForm("Limited Company");
            dataDao.setGoverningLaw("Act of law");
            dataDao.setLegalEntityRegisterName("UK Register");
            dataDao.setLegalEntityRegistrationLocation("United Kingdom");
            dataDao.setContributionCurrencyType("GBP");
            dataDao.setRegisteredCompanyNumber("12345678");

            dao.setData(dataDao);
            dao.setId(SUBMISSION_ID);

            return dao;
        }
    }

    @Nested
    class CreateLimitedPartnerPerson {

        @Test
        void shouldCreateALimitedPartnerPerson() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerPersonDto();
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
            assertEquals(SUBMISSION_ID, submissionId);

            String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), SUBMISSION_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldFailCreateALimitedPartnerPersonIfForenameIsCorrectAndOthersAreNull() {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerPersonDto();
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
        void shouldFailCreateALimitedPartnerPersonIfSurnameIsCorrectAndOthersAreNull() {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerPersonDto();
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
        void shouldFailCreateALimitedPartnerPersonIfNationality1AndNationality2AreSame() {
            Transaction transaction = buildTransaction();
            LimitedPartnerDto dto = createLimitedPartnerPersonDto();
            dto.getData().setNationality2(Nationality.AMERICAN);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createLimitedPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("nationality1"));
            assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
        }

        private LimitedPartnerDto createLimitedPartnerPersonDto() {
            LimitedPartnerDto dto = new LimitedPartnerDto();

            LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
            dataDto.setForename("John");
            dataDto.setSurname("Doe");
            dataDto.setDateOfBirth(LocalDate.of(1980, 1, 1));
            dataDto.setNationality1(Nationality.AMERICAN);
            dataDto.setContributionCurrencyType(Currency.GBP);

            dto.setData(dataDto);

            return dto;
        }

        private LimitedPartnerDao createLimitedPartnerPersonDao() {
            LimitedPartnerDao dao = new LimitedPartnerDao();

            LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
            dataDao.setForename("John");
            dataDao.setSurname("Doe");
            dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
            dataDao.setNationality1("American");
            dataDao.setContributionCurrencyType("British Pound");

            dao.setData(dataDao);
            dao.setId(SUBMISSION_ID);

            return dao;
        }
    }

    @Test
    void shouldFailCreateALimitedPartnerPersonIfAllFieldsAreNull() {
        Transaction transaction = buildTransaction();
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
}
