package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnerServiceUpdateTest {
    private static final String TRANSACTION_ID = "863851-951242-143528";
    private static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String USER_ID = "xbJf0l";

    private Transaction transaction;

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

    private LimitedPartnerDao createLimitedPartnerPersonDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setForename("John");
        dataDao.setSurname("Doe");
        dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
        dataDao.setNationality1("American");

        dao.setData(dataDao);
        dao.setId(LIMITED_PARTNER_ID);

        return dao;
    }

    private LimitedPartnerDao createLimitedPartnerLegalEntityDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");

        dao.setData(dataDao);
        dao.setId(LIMITED_PARTNER_ID);

        return dao;
    }

    private Transaction buildTransaction() {
        Transaction trx = new Transaction();
        trx.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNER);

        String uri = String.format(URL_GET_LIMITED_PARTNER, TRANSACTION_ID, LIMITED_PARTNER_ID);

        Map<String, String> links = new HashMap<>();
        links.put("resource", uri);
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(uri, resource);
        trx.setResources(resourceMap);

        return trx;
    }

    @BeforeEach
    void setUp() {
        transaction = buildTransaction();
    }

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();
        limitedPartnerDao.getData().setNationality2(Nationality.GREENLANDIC.getDescription());

        AddressDto principalOfficeAddress = new AddressDto();
        principalOfficeAddress.setAddressLine1("DUNCALF STREET");
        principalOfficeAddress.setCountry("England");
        principalOfficeAddress.setLocality("STOKE-ON-TRENT");
        principalOfficeAddress.setPostalCode("ST6 3LJ");
        principalOfficeAddress.setPremises("2");

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setPrincipalOfficeAddress(principalOfficeAddress);

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        // dao principal office address is null before mapping/update
        assertNull(limitedPartnerDao.getData().getPrincipalOfficeAddress());

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(LIMITED_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

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
    void shouldUpdateTheDaoWithCapitalContributions() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setContributionCurrencyType(Currency.GBP);
        limitedPartnerDataDto.setContributionCurrencyValue("15.00");
        List<ContributionSubTypes> contributionSubtypes = new ArrayList<>();
        contributionSubtypes.add(ContributionSubTypes.MONEY);
        contributionSubtypes.add(ContributionSubTypes.SERVICES_OR_GOODS);
        limitedPartnerDataDto.setContributionSubTypes(contributionSubtypes);

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dataDto = new DataDto();
        dataDto.setPartnershipType(PartnershipType.LP);
        limitedPartnershipDto.setData(dataDto);

        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(limitedPartnershipDto);

        transaction.setFilingMode(IncorporationKind.REGISTRATION.getDescription());

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).findById(LIMITED_PARTNER_ID);
        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Currency.GBP, sentSubmission.getData().getContributionCurrencyType());
        assertEquals("15.00", sentSubmission.getData().getContributionCurrencyValue());
        assertThat(sentSubmission.getData().getContributionSubTypes())
                .contains(ContributionSubTypes.MONEY, ContributionSubTypes.SERVICES_OR_GOODS);
    }

    @Test
    void shouldFailUpdateIfNationalitiesAreTheSame() {
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setNationality1(Nationality.AMERICAN);
        limitedPartnerDataDto.setNationality2(Nationality.AMERICAN);

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID)
        );

        assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
    }

    @Test
    void shouldAllowUpdateIfNationalitiesAreDifferent() throws Exception {
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setNationality1(Nationality.AMERICAN);
        limitedPartnerDataDto.setNationality2(Nationality.NEW_ZEALANDER);

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertEquals(Nationality.NEW_ZEALANDER.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setNationality1(Nationality.AMERICAN);
        limitedPartnerDataDto.setNationality2(null);

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

        service.updateLimitedPartner(transaction, LIMITED_PARTNER_ID, limitedPartnerDataDto, REQUEST_ID, USER_ID);

        verify(limitedPartnerRepository).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertNull(sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithLegalEntityRegistrationLocation() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerLegalEntityDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(limitedPartnerRepository.findById(limitedPartnerDao.getId())).thenReturn(Optional.of(limitedPartnerDao));

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
        LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

        LimitedPartnerDataDto limitedPartnerDataDto = new LimitedPartnerDataDto();
        limitedPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

        assertThatThrownBy(() -> service.updateLimitedPartner(transaction, "LIMITED_PARTNER_ID_NOT_SAME_TRANSACTION", limitedPartnerDataDto, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches limited partner id: %s", transaction.getId(), "LIMITED_PARTNER_ID_NOT_SAME_TRANSACTION"));
    }

    @Nested
    class DeleteLimitedPartner {
        @Test
        void shouldDeleteLimitedPartner() throws ServiceException {
            LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

            service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID);

            String expectedSubmissionUri = String.format(URL_GET_LIMITED_PARTNER, TRANSACTION_ID, LIMITED_PARTNER_ID);

            verify(transactionService).deleteTransactionResource(TRANSACTION_ID, expectedSubmissionUri, REQUEST_ID);
            verify(limitedPartnerRepository).deleteById(LIMITED_PARTNER_ID);
        }

        @Test
        void shouldNotDeleteLimitedPartnerIfTransactionResourceDeleteFails() throws ServiceException {
            LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();
            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

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

            assertThatThrownBy(() -> service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.format("Limited partner with id %s not found", LIMITED_PARTNER_ID));
        }

        @Test
        void testDeleteLimitedPartnerLinkFails() {
            LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

            assertThatThrownBy(() -> service.deleteLimitedPartner(transaction, "LIMITED_PARTNER_ID_NOT_SAME_TRANSACTION", REQUEST_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches limited partner id: %s", transaction.getId(), "LIMITED_PARTNER_ID_NOT_SAME_TRANSACTION"));
        }
    }
}
