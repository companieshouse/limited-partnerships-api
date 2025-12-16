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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GeneralPartnerServiceUpdateTest {
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String GENERAL_PARTNER_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_GENERAL_PARTNER,
            URL_GET_GENERAL_PARTNER,
            GENERAL_PARTNER_ID
    ).build();

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

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().legalEntityDao();
        generalPartnerDao.getData().setNationality2(Nationality.GREENLANDIC.getDescription());
        generalPartnerDao.getData().setPrincipalOfficeAddress(null);

        GeneralPartnerDto generalPartnerDto = new GeneralPartnerBuilder().legalEntityDto();
        generalPartnerDto.getData().setDateEffectiveFrom(null);

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(generalPartnerDao.getData().getPrincipalOfficeAddress());

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDto.getData(), REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(generalPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        AddressDto principalOfficeAddress = generalPartnerDto.getData().getPrincipalOfficeAddress();

        assertEquals(generalPartnerDao.getData().getForename(), sentSubmission.getData().getForename());
        assertEquals(principalOfficeAddress.getAddressLine1(), sentSubmission.getData().getPrincipalOfficeAddress().getAddressLine1());
        assertEquals(principalOfficeAddress.getCountry(), sentSubmission.getData().getPrincipalOfficeAddress().getCountry());
        assertEquals(principalOfficeAddress.getLocality(), sentSubmission.getData().getPrincipalOfficeAddress().getLocality());
        assertEquals(principalOfficeAddress.getPostalCode(), sentSubmission.getData().getPrincipalOfficeAddress().getPostalCode());
        assertEquals(principalOfficeAddress.getPremises(), sentSubmission.getData().getPrincipalOfficeAddress().getPremises());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldFailUpdateIfNationalitiesAreTheSame() {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().personDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
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
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().personDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
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
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().personDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
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

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void shouldHandleUsualResidentialAddressForUpdateUraRequiredFlag(Boolean uraRequired) throws Exception {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().withGeneralPartnerKind(
                PartnerKind.UPDATE_GENERAL_PARTNER_PERSON.getDescription()).personDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().personDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
        generalPartnerDataDto.setUpdateUsualResidentialAddressRequired(uraRequired);

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        if (uraRequired) {
            assertTrue(sentSubmission.getData().getUpdateUsualResidentialAddressRequired());
            assertNotNull(sentSubmission.getData().getUsualResidentialAddress());
        } else {
            assertFalse(sentSubmission.getData().getUpdateUsualResidentialAddressRequired());
            assertNull(sentSubmission.getData().getUsualResidentialAddress());
        }
    }

    @Test
    void shouldUpdateTheDaoWithLegalEntityRegistrationLocation() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().legalEntityDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().legalEntityDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
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
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().personDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
        generalPartnerDataDto.setNotDisqualifiedStatementChecked(input);

        when(generalPartnerRepository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateGeneralPartner(transaction, GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(generalPartnerRepository).findById(GENERAL_PARTNER_ID);
        verify(generalPartnerRepository).save(submissionCaptor.capture());

        GeneralPartnerDao newlySavedPartnerDao = submissionCaptor.getValue();

        assertEquals(input == null || input, newlySavedPartnerDao.getData().getNotDisqualifiedStatementChecked());
    }

    @Test
    void testGeneralUpdateLimitedPartnerLinkFails() {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().legalEntityDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerBuilder().legalEntityDto().getData();
        generalPartnerDataDto.setDateEffectiveFrom(null);
        generalPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.updateGeneralPartner(transaction, generalPartnerDao.getId(), generalPartnerDataDto, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format("Transaction id: %s does not have a resource that matches general partner id: %s", transaction.getId(), generalPartnerDao.getId()));
    }

    @Nested
    class DeleteGeneralPartner {
        @Test
        void shouldDeleteGeneralPartner() throws ServiceException {
            GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

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
            GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

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
            GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

            when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));

            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(false);

            assertThatThrownBy(() -> service.deleteGeneralPartner(transaction, generalPartnerDao.getId(), REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(String.format("Transaction id: %s does not have a resource that matches general partner id: %s", transaction.getId(), generalPartnerDao.getId()));
        }
    }
}
