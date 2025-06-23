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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GeneralPartnerServiceCreateTest {

    private static final String USER_ID = "xbJf0l";
    private static final String GENERAL_PARTNER_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String TRANSACTION_ID = "txn-456";

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
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

    @Nested
    class CreateGeneralPartnerLegalEntity {
        @Test
        void shouldCreateAGeneralPartnerLegalEntity() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            GeneralPartnerDto dto = createGeneralPartnerLegalEntityDto();
            GeneralPartnerDao dao = createGeneralPartnerLegalEntityDao();

            when(repository.insert((GeneralPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            String submissionId = service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            GeneralPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(FILING_KIND_GENERAL_PARTNER, sentSubmission.getData().getKind());
            assertEquals(GENERAL_PARTNER_ID, submissionId);

            String expectedUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), GENERAL_PARTNER_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldFailCreateAGeneralPartnerLegalEntityIfLegalEntityRegisterNameIsCorrectAndOthersAreNull() {
            GeneralPartnerDto dto = createGeneralPartnerLegalEntityDto();
            var data = dto.getData();
            data.setLegalEntityName(null);
            data.setLegalForm(null);
            data.setGoverningLaw(null);
            data.setLegalEntityRegistrationLocation(null);
            data.setRegisteredCompanyNumber(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("legal_entity_register_name"));
            assertEquals("Legal Entity Name is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_name")).getDefaultMessage());
            assertEquals("Legal Form is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_form")).getDefaultMessage());
            assertEquals("Governing Law is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
            assertEquals("Legal Entity Registration Location is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
            assertEquals("Registered Company Number is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateAGeneralPartnerLegalEntityIfLegalFormIsCorrectAndOthersAreNull() {
            GeneralPartnerDto dto = createGeneralPartnerLegalEntityDto();
            var data = dto.getData();

            data.setLegalEntityName(null);
            data.setGoverningLaw(null);
            data.setLegalEntityRegisterName(null);
            data.setLegalEntityRegistrationLocation(null);
            data.setRegisteredCompanyNumber(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("legal_form"));
            assertEquals("Legal Entity Name is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_name")).getDefaultMessage());
            assertEquals("Governing Law is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
            assertEquals("Legal Entity Register Name is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_register_name")).getDefaultMessage());
            assertEquals("Legal Entity Registration Location is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
            assertEquals("Registered Company Number is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
        }

        private GeneralPartnerDto createGeneralPartnerLegalEntityDto() {
            GeneralPartnerDto dto = new GeneralPartnerDto();

            GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
            dataDto.setLegalEntityName("Legal Entity Name");
            dataDto.setLegalForm("Form");
            dataDto.setGoverningLaw("Act of law");
            dataDto.setLegalEntityRegisterName("Register of United States");
            dataDto.setLegalEntityRegistrationLocation(Country.UNITED_STATES);
            dataDto.setRegisteredCompanyNumber("12345678");

            dto.setData(dataDto);
            return dto;
        }

        private GeneralPartnerDao createGeneralPartnerLegalEntityDao() {
            GeneralPartnerDao dao = new GeneralPartnerDao();

            GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
            dataDao.setLegalEntityName("My company ltd");
            dataDao.setLegalForm("Limited Company");
            dataDao.setGoverningLaw("Act of law");
            dataDao.setLegalEntityRegisterName("UK Register");
            dataDao.setLegalEntityRegistrationLocation("United Kingdom");
            dataDao.setRegisteredCompanyNumber("12345678");
            dataDao.setNotDisqualifiedStatementChecked(true);

            dao.setData(dataDao);
            dao.setId(GENERAL_PARTNER_ID);

            return dao;
        }
    }

    @Nested
    class CreateGeneralPartnerPerson {

        @Test
        void shouldCreateAGeneralPartnerPerson() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
            mocks();

            GeneralPartnerDto dto = createGeneralPartnerPersonDto();
            dto.getData().setNationality2(Nationality.BRITISH);
            GeneralPartnerDao dao = createGeneralPartnerPersonDao();
            dao.getData().setNationality2("British");

            when(repository.insert((GeneralPartnerDao) any())).thenReturn(dao);
            when(repository.save(dao)).thenReturn(dao);

            String submissionId = service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

            verify(repository).insert(submissionCaptor.capture());

            GeneralPartnerDao sentSubmission = submissionCaptor.getValue();
            assertEquals(USER_ID, sentSubmission.getCreatedBy());
            assertEquals(FILING_KIND_GENERAL_PARTNER, sentSubmission.getData().getKind());
            assertEquals(GENERAL_PARTNER_ID, submissionId);

            String expectedUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), GENERAL_PARTNER_ID);
            assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
        }

        @Test
        void shouldFailCreateAGeneralPartnerPersonIfForenameIsCorrectAndOthersAreNull() throws ServiceException {
            mocks();

            GeneralPartnerDto dto = createGeneralPartnerPersonDto();
            dto.getData().setSurname(null);
            dto.getData().setDateOfBirth(null);
            dto.getData().setNationality1(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("forename"));
            assertEquals("Surname is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("surname")).getDefaultMessage());
            assertEquals("Date of birth is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("date_of_birth")).getDefaultMessage());
            assertEquals("Nationality1 is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality1")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateAGeneralPartnerPersonIfSurnameIsCorrectAndOthersAreNull() throws ServiceException {
            mocks();

            GeneralPartnerDto dto = createGeneralPartnerPersonDto();
            dto.getData().setForename(null);
            dto.getData().setDateOfBirth(null);
            dto.getData().setNationality1(null);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("surname"));
            assertEquals("Forename is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("forename")).getDefaultMessage());
            assertEquals("Date of birth is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("date_of_birth")).getDefaultMessage());
            assertEquals("Nationality1 is required", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality1")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateAGeneralPartnerPersonIfNationality1AndNationality2AreSame() throws ServiceException {
            mocks();

            GeneralPartnerDto dto = createGeneralPartnerPersonDto();
            dto.getData().setNationality2(Nationality.AMERICAN);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertNull(exception.getBindingResult().getFieldError("nationality1"));
            assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
        }

        @Test
        void shouldFailCreateAGeneralPartnerPersonIfNotDisqualifiedStatementCheckedIsFalse() {
            GeneralPartnerDto dto = createGeneralPartnerPersonDto();
            var data = dto.getData();
            data.setNotDisqualifiedStatementChecked(false);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                    service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
            );

            assertEquals("Not Disqualified Statement must be checked", Objects.requireNonNull(exception.getBindingResult().getFieldError(NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD)).getDefaultMessage());
        }

        private GeneralPartnerDto createGeneralPartnerPersonDto() {
            GeneralPartnerDto dto = new GeneralPartnerDto();

            GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
            dataDto.setForename("John");
            dataDto.setSurname("Doe");
            dataDto.setDateOfBirth(LocalDate.of(1980, 1, 1));
            dataDto.setNationality1(Nationality.AMERICAN);
            dataDto.setNotDisqualifiedStatementChecked(true);

            dto.setData(dataDto);

            return dto;
        }

        private GeneralPartnerDao createGeneralPartnerPersonDao() {
            GeneralPartnerDao dao = new GeneralPartnerDao();

            GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
            dataDao.setForename("John");
            dataDao.setSurname("Doe");
            dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
            dataDao.setNationality1("American");
            dataDao.setNotDisqualifiedStatementChecked(true);

            dao.setData(dataDao);
            dao.setId(GENERAL_PARTNER_ID);

            return dao;
        }
    }

    @Test
    void shouldFailCreateAGeneralPartnerPersonIfAllFieldsAreNull() {
        GeneralPartnerDto dto = new GeneralPartnerDto();
        GeneralPartnerDataDto dataDao = new GeneralPartnerDataDto();
        dto.setData(dataDao);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
        );

        assertNull(exception.getBindingResult().getFieldError("forename"));
        assertNull(exception.getBindingResult().getFieldError("surname"));
        assertNull(exception.getBindingResult().getFieldError("legal_entity_register_name"));
        assertNull(exception.getBindingResult().getFieldError("legal_form"));
        assertEquals("Some fields are missing", Objects.requireNonNull(exception.getBindingResult().getFieldError("")).getDefaultMessage());
    }

    private void mocks(GeneralPartnerDao limitedDao) {
        when(repository.insert((GeneralPartnerDao) any())).thenReturn(limitedDao);
        when(repository.save(any())).thenReturn(limitedDao);
        when(repository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(limitedDao));
        doNothing().when(repository).deleteById(GENERAL_PARTNER_ID);
    }

    private void mocks() {
        GeneralPartnerDao limitedDao = new GeneralPartnerBuilder().dao();

        mocks(limitedDao);
    }
}
