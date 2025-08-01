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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GeneralPartnerServiceUpdateTest {
    private static final String TRANSACTION_ID = "863851-951242-143528";
    private static final String LIMITED_PARTNERSHIP_ID = "687690b4a42b65054d2ef0e3";
    private static final String GENERAL_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    Transaction transaction = buildTransaction();

    @Autowired
    private GeneralPartnerService service;

    @MockitoBean
    private GeneralPartnerRepository generalPartnerRepository;

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

        dao.setData(dataDao);
        dao.setId(GENERAL_PARTNER_ID);

        return dao;
    }

    private Transaction buildTransaction() {
        Transaction trx = new Transaction();
        trx.setId(TRANSACTION_ID);

        Resource masterResource = new Resource();
        masterResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_GENERAL_PARTNER);

        String masterUri = String.format(URL_GET_PARTNERSHIP, TRANSACTION_ID, LIMITED_PARTNERSHIP_ID);
        String uri = String.format(URL_GET_GENERAL_PARTNER, TRANSACTION_ID, GENERAL_PARTNER_ID);

        Map<String, String> masterLinks = new HashMap<>();
        masterLinks.put("masterResource", masterUri);
        resource.setLinks(masterLinks);
        Map<String, String> links = new HashMap<>();
        links.put("resource", uri);
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(masterUri, masterResource);
        resourceMap.put(uri, resource);
        trx.setResources(resourceMap);

        return trx;
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

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(generalPartnerDao.getData().getPrincipalOfficeAddress());

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(generalPartnerRepository).save(submissionCaptor.capture());

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

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID)
        );

        assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
    }

    @Test
    void shouldAllowUpdateIfNationalitiesAreDifferent() throws Exception {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(Nationality.NEW_ZEALANDER);

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).save(submissionCaptor.capture());

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

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertNull(sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithLegalEntityRegistrationLocation() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerLegalEntityDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        // dao principal office address before mapping/update
        assertEquals("United Kingdom", generalPartnerDao.getData().getLegalEntityRegistrationLocation());

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(generalPartnerRepository).save(submissionCaptor.capture());

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

        when(generalPartnerRepository.findById(currentlySavedPartnerDao.getId())).thenReturn(Optional.of(currentlySavedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, partnerDataDtoWithChanges, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(generalPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao newlySavedPartnerDao = submissionCaptor.getValue();

        assertEquals(input == null || input, newlySavedPartnerDao.getData().getNotDisqualifiedStatementChecked());
    }

    @Test
    void testGeneralUpdateLimitedPartnerLinkFails() {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerLegalEntityDao();

        GeneralPartnerDataDto partnerDataDtoWithChanges = new GeneralPartnerDataDto();
        partnerDataDtoWithChanges.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.updateGeneralPartner(transaction, "GENERAL_PARTNER_ID_NOT_SAME_TRANSACTION", partnerDataDtoWithChanges, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format("Transaction id: %s does not have a resource that matches general partner id: %s", transaction.getId(), "GENERAL_PARTNER_ID_NOT_SAME_TRANSACTION"));
    }

    @Nested
    class DeleteGeneralPartner {
        @Test
        void shouldDeleteGeneralPartner() throws ServiceException {
            GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

            when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

            service.deleteGeneralPartner(transaction, GENERAL_PARTNER_ID, REQUEST_ID);

            String expectedSubmissionUri = String.format(URL_GET_GENERAL_PARTNER, TRANSACTION_ID, GENERAL_PARTNER_ID);

            verify(transactionService).deleteTransactionResource(TRANSACTION_ID, expectedSubmissionUri, REQUEST_ID);
            verify(generalPartnerRepository).deleteById(GENERAL_PARTNER_ID);
        }

        @Test
        void shouldThrowServiceExceptionWhenGeneralPartnerNotFound() {
            when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.empty());
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> service.deleteGeneralPartner(transaction, GENERAL_PARTNER_ID, REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(String.format("General partner with id %s not found", GENERAL_PARTNER_ID));
        }

        @Test
        void shouldNotDeleteGeneralPartnerIfTransactionResourceDeleteFails() throws ServiceException {
            GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();
            when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

            doThrow(new ServiceException("Transaction resource delete failed"))
                    .when(transactionService).deleteTransactionResource(TRANSACTION_ID,
                            String.format(URL_GET_GENERAL_PARTNER, TRANSACTION_ID, GENERAL_PARTNER_ID), REQUEST_ID);

            assertThatThrownBy(() -> service.deleteGeneralPartner(transaction, GENERAL_PARTNER_ID, REQUEST_ID))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Transaction resource delete failed");

            verify(generalPartnerRepository, never()).deleteById(GENERAL_PARTNER_ID);
        }

        @Test
        void testDeleteGeneralPartnerLinkFails() {
            GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

            when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(false);

            assertThatThrownBy(() -> service.deleteGeneralPartner(transaction, "GENERAL_PARTNER_ID_NOT_SAME_TRANSACTION", REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(String.format("Transaction id: %s does not have a resource that matches general partner id: %s", transaction.getId(), "GENERAL_PARTNER_ID_NOT_SAME_TRANSACTION"));
        }
    }
}
