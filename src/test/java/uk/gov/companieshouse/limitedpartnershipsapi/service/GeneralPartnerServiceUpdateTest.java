package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GeneralPartnerServiceUpdateTest {
    private static final String GENERAL_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Autowired
    private GeneralPartnerService service;

    @MockitoBean
    private GeneralPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

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

    private GeneralPartnerDao createGeneralPartnerLegalEntityDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");
        dataDao.setLegalPersonalityStatementChecked(true);

        dao.setData(dataDao);
        dao.setId(GENERAL_PARTNER_ID);

        return dao;
    }

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();
        generalPartnerDao.getData().setNationality2(Nationality.GREENLANDIC.getDescription());

        AddressDto principalOfficeAddress = new AddressDto();
        principalOfficeAddress.setAddressLine1("DUNCALF STREET");
        principalOfficeAddress.setCountry("England");
        principalOfficeAddress.setLocality("STOKE-ON-TRENT");
        principalOfficeAddress.setPostalCode("ST6 3LJ");
        principalOfficeAddress.setPremises("2");

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setPrincipalOfficeAddress(principalOfficeAddress);

        when(limitedPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // dao principal office address is null before mapping/update
        assertNull(generalPartnerDao.getData().getPrincipalOfficeAddress());

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals("John", sentSubmission.getData().getForename());
        assertEquals("DUNCALF STREET", sentSubmission.getData().getPrincipalOfficeAddress().getAddressLine1());
        assertEquals("England", sentSubmission.getData().getPrincipalOfficeAddress().getCountry());
        assertEquals("STOKE-ON-TRENT", sentSubmission.getData().getPrincipalOfficeAddress().getLocality());
        assertEquals("ST6 3LJ", sentSubmission.getData().getPrincipalOfficeAddress().getPostalCode());
        assertEquals("2", sentSubmission.getData().getPrincipalOfficeAddress().getPremises());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldFailUpdateIfNationalitiesAreTheSame() {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(Nationality.AMERICAN);

        when(limitedPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID)
        );

        assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
    }

    @Test
    void shouldAllowUpdateIfNationalitiesAreDifferent() throws Exception {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(Nationality.NEW_ZEALANDER);

        when(limitedPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertEquals(Nationality.NEW_ZEALANDER.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(null);

        when(limitedPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertNull(sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithLegalEntityRegistrationLocation() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerLegalEntityDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(limitedPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // dao principal office address before mapping/update
        assertEquals("United Kingdom", generalPartnerDao.getData().getLegalEntityRegistrationLocation());

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals("England", sentSubmission.getData().getLegalEntityRegistrationLocation());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void shouldCorrectlyUpdateDisqualificationStatementCheckedValue(Boolean input) throws Exception {
        GeneralPartnerDao currentlySavedPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto partnerDataDtoWithChanges = new GeneralPartnerDataDto();
        partnerDataDtoWithChanges.setNotDisqualifiedStatementChecked(input);

        when(limitedPartnerRepository.findById(currentlySavedPartnerDao.getId())).thenReturn(Optional.of(currentlySavedPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, partnerDataDtoWithChanges, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao newlySavedPartnerDao = submissionCaptor.getValue();

        assertEquals(input == null || input, newlySavedPartnerDao.getData().getNotDisqualifiedStatementChecked());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void shouldCorrectlyUpdateLegalPersonalityStatementCheckedValue(Boolean input) throws Exception {
        GeneralPartnerDao currentlySavedPartnerDao = createGeneralPartnerLegalEntityDao();

        GeneralPartnerDataDto partnerDataDtoWithChanges = new GeneralPartnerDataDto();
        partnerDataDtoWithChanges.setLegalPersonalityStatementChecked(input);

        when(limitedPartnerRepository.findById(currentlySavedPartnerDao.getId())).thenReturn(Optional.of(currentlySavedPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, partnerDataDtoWithChanges, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao newlySavedPartnerDao = submissionCaptor.getValue();

        assertEquals(input == null || input, newlySavedPartnerDao.getData().getLegalPersonalityStatementChecked());
    }

    @Nested
    class DeleteGeneralPartner {
        private static final String GENERAL_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
        private static final String TRANSACTION_ID = "863851-951242-143528";

        @Test
        void shouldDeleteGeneralPartner() throws ServiceException {
            Transaction transaction = buildTransaction();

            GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

            // transaction before
            assertEquals(1, transaction.getResources().size());

            when(limitedPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));

            service.deleteGeneralPartner(transaction, GENERAL_PARTNER_ID, REQUEST_ID);

            verify(transactionService).updateTransaction(transactionCaptor.capture(), eq(REQUEST_ID));

            Transaction transactionUpdated = transactionCaptor.getValue();

            assertEquals(0, transactionUpdated.getResources().size());

            // transaction after
            assertEquals(0, transaction.getResources().size());
        }

        @Test
        void shouldThrowServiceExceptionWhenGeneralPartnerNotFound() {
            Transaction transaction = buildTransaction();

            when(limitedPartnerRepository.findById("wrong-id")).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    service.deleteGeneralPartner(transaction, "wrong-id", REQUEST_ID)
            );

            assertEquals("General partner with id wrong-id not found", exception.getMessage());
        }

        private Transaction buildTransaction() {
            Transaction transaction = new Transaction();
            transaction.setId(TRANSACTION_ID);

            Resource resource = new Resource();
            resource.setKind(FILING_KIND_GENERAL_PARTNER);
            Map<String, String> links = new HashMap<>();
            links.put("resource", String.format("/transactions/%s/limited-partnership/general-partner/%s", TRANSACTION_ID, GENERAL_PARTNER_ID));
            resource.setLinks(links);

            Map<String, Resource> resourceMap = new HashMap<>();
            resourceMap.put(String.format("/transactions/%s/limited-partnership/general-partner/%s", TRANSACTION_ID, GENERAL_PARTNER_ID), resource);
            transaction.setResources(resourceMap);

            return transaction;
        }
    }
}
