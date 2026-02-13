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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnerServiceUpdateTest {
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String LIMITED_PARTNER_ID = LimitedPartnerBuilder.LIMITED_PARTNER_ID;
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String USER_ID = "xbJf0l";

    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LIMITED_PARTNER_ID
    ).build();

    @Autowired
    private LimitedPartnerService service;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<LimitedPartnerDao> submissionCaptor;

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().legalEntityDao();
        limitedPartnerDao.getData().setNationality2(Nationality.GREENLANDIC.getDescription());
        limitedPartnerDao.getData().setPrincipalOfficeAddress(null);

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().legalEntityDto().getData();
        limitedPartnerDataDto.setDateEffectiveFrom(null);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(limitedPartnerDao.getData().getPrincipalOfficeAddress());

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(LIMITED_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        AddressDto principalOfficeAddress = limitedPartnerDataDto.getPrincipalOfficeAddress();

        assertEquals(limitedPartnerDataDto.getForename(), sentSubmission.getData().getForename());
        assertEquals(principalOfficeAddress.getAddressLine1(), sentSubmission.getData().getPrincipalOfficeAddress().getAddressLine1());
        assertEquals(principalOfficeAddress.getCountry(), sentSubmission.getData().getPrincipalOfficeAddress().getCountry());
        assertEquals(principalOfficeAddress.getLocality(), sentSubmission.getData().getPrincipalOfficeAddress().getLocality());
        assertEquals(principalOfficeAddress.getPostalCode(), sentSubmission.getData().getPrincipalOfficeAddress().getPostalCode());
        assertEquals(principalOfficeAddress.getPremises(), sentSubmission.getData().getPrincipalOfficeAddress().getPremises());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithCapitalContributions() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().personDto().getData();

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(limitedPartnershipDto);
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(LIMITED_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Currency.GBP, sentSubmission.getData().getContributionCurrencyType());
        assertEquals("1000.00", sentSubmission.getData().getContributionCurrencyValue());
        assertThat(sentSubmission.getData().getContributionSubTypes())
                .contains(ContributionSubTypes.SHARES, ContributionSubTypes.SHARES);
    }

    @Test
    void shouldFailUpdateIfNationalitiesAreTheSame() {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().personDto().getData();
        limitedPartnerDataDto.setContributionSubTypes(null);
        limitedPartnerDataDto.setNationality1(Nationality.AMERICAN);
        limitedPartnerDataDto.setNationality2(Nationality.AMERICAN);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID)
        );

        assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
    }

    @Test
    void shouldAllowUpdateIfNationalitiesAreDifferent() throws Exception {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().personDto().getData();
        limitedPartnerDataDto.setContributionSubTypes(null);
        limitedPartnerDataDto.setNationality1(Nationality.AMERICAN);
        limitedPartnerDataDto.setNationality2(Nationality.NEW_ZEALANDER);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertEquals(Nationality.NEW_ZEALANDER.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().personDto().getData();
        limitedPartnerDataDto.setContributionSubTypes(null);
        limitedPartnerDataDto.setNationality1(Nationality.AMERICAN);
        limitedPartnerDataDto.setNationality2(null);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertNull(sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithLegalEntityRegistrationLocation() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().legalEntityDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().legalEntityDto().getData();
        limitedPartnerDataDto.setContributionSubTypes(null);
        limitedPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        // dao principal office address before mapping/update
        assertEquals("United Kingdom", limitedPartnerDao.getData().getLegalEntityRegistrationLocation());

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(LIMITED_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals("England", sentSubmission.getData().getLegalEntityRegistrationLocation());
    }

    @Test
    void testLimitedPartnerUpdateLimitedPartnerLinkFails() {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerBuilder().personDto().getData();
        limitedPartnerDataDto.setContributionSubTypes(null);
        limitedPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

        assertThatThrownBy(() -> service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches limited partner id: %s", transaction.getId(), LIMITED_PARTNER_ID));
    }

    @Test
    void shouldValidateUpdateWithLegalEntityKindWhenDataHasNoLegalEntityFields() {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder()
                .withLimitedPartnerKind(PartnerKind.UPDATE_LIMITED_PARTNER_LEGAL_ENTITY.getDescription())
                .personDao();
        limitedPartnerDao.getData().setContributionCurrencyValue(null);
        limitedPartnerDao.getData().setContributionCurrencyType(null);
        limitedPartnerDao.getData().setContributionSubTypes(null);

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID)
        );

        assertThat(exception.getBindingResult().getFieldErrors()).anyMatch(
                e -> e.getDefaultMessage().contains("Legal Entity Name is required")
        );
    }

    @Test
    void shouldValidateContributionsWhenOnlyCurrencyTypeIsPresent() throws Exception {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();
        limitedPartnerDao.getData().setContributionCurrencyValue(null);
        limitedPartnerDao.getData().setContributionSubTypes(null);

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(Currency.GBP, sentSubmission.getData().getContributionCurrencyType());
        assertNull(sentSubmission.getData().getContributionCurrencyValue());
    }

    @Test
    void shouldValidateContributionsWhenOnlySubTypesArePresent() throws Exception {
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();
        limitedPartnerDao.getData().setContributionCurrencyValue(null);
        limitedPartnerDao.getData().setContributionCurrencyType(null);

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));
        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
        assertThat(sentSubmission.getData().getContributionSubTypes()).contains(ContributionSubTypes.SHARES);
        assertNull(sentSubmission.getData().getContributionCurrencyValue());
        assertNull(sentSubmission.getData().getContributionCurrencyType());
    }

    @Nested
    class DeleteLimitedPartner {
        @Test
        void shouldDeleteLimitedPartner() throws ServiceException {
            LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

            service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID);

            String expectedSubmissionUri = String.format(URL_GET_LIMITED_PARTNER, TRANSACTION_ID, LIMITED_PARTNER_ID);

            verify(transactionService).deleteTransactionResource(TRANSACTION_ID, expectedSubmissionUri, REQUEST_ID);
            verify(limitedPartnerRepository).deleteById(LIMITED_PARTNER_ID);
        }

        @Test
        void shouldNotDeleteLimitedPartnerIfTransactionResourceDeleteFails() throws ServiceException {
            LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

            doThrow(new ServiceException("Transaction resource delete failed"))
                    .when(transactionService).deleteTransactionResource(TRANSACTION_ID,
                            String.format(URL_GET_LIMITED_PARTNER, TRANSACTION_ID, LIMITED_PARTNER_ID), REQUEST_ID);

            assertThatThrownBy(() -> service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Transaction resource delete failed");

            verify(limitedPartnerRepository, never()).deleteById(LIMITED_PARTNER_ID);
        }

        @Test
        void shouldThrowServiceExceptionWhenLimitedPartnerNotFound() {
            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.empty());
            when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.format("Limited partner with id %s not found", LIMITED_PARTNER_ID));
        }

        @Test
        void testDeleteLimitedPartnerLinkFails() {
            LimitedPartnerDao limitedPartnerDao = new LimitedPartnerBuilder().personDao();

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

            assertThatThrownBy(() -> service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches limited partner id: %s", transaction.getId(), LIMITED_PARTNER_ID));
        }
    }
}
