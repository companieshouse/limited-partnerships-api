package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficerGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveDateOfBirthAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.FilingKind;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder.GENERAL_PARTNER_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder.LIMITED_PARTNER_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder.COMPANY_NUMBER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_PAYMENT_METHOD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_PAYMENT_REFERENCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.GENERAL_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNER_FIELD;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class FilingsServiceTest {

    private static final String INCORPORATION_ID = "inc456";
    private static final String PAYMENT_METHOD = "credit-card";
    private static final String PAYMENT_REFERENCE = "21311sfg23";

    @Autowired
    private FilingsService filingsService;
    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;
    @MockitoBean
    private GeneralPartnerService generalPartnerService;
    @MockitoBean
    private LimitedPartnerService limitedPartnerService;
    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private ApiClientService apiClientService;
    @MockitoBean
    private InternalApiClient internalApiClient;
    @MockitoBean
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @MockitoBean
    private PrivateOfficerGet privateOfficerGet;
    @MockitoBean
    private ApiResponse<AppointmentFullRecordAPI> appointmentFullRecordAPIApiResponse;

    @Test
    void testFilingGenerationSuccess() throws ServiceException {
        var transaction = new TransactionBuilder().withPayment().build();
        PaymentApi payment = new PaymentApi();
        payment.setPaymentMethod(PAYMENT_METHOD);

        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(transactionService.getPaymentReference(transaction.getLinks().getPayment())).thenReturn(PAYMENT_REFERENCE);
        when(paymentService.getPayment(PAYMENT_REFERENCE)).thenReturn(payment);
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(new LimitedPartnershipBuilder().buildDto());
        when(generalPartnerService.getGeneralPartnerDataList(transaction)).thenReturn(Collections.singletonList(new GeneralPartnerBuilder().personDto().getData()));
        when(limitedPartnerService.getLimitedPartnerDataList(transaction)).thenReturn(Collections.singletonList(new LimitedPartnerBuilder().legalEntityDto().getData()));

        FilingApi filing = filingsService.generateIncorporationFiling(transaction, INCORPORATION_ID);

        assertNotNull(filing);
        assertNotNull(filing.getData());
        assertTrue(filing.getData().containsKey(LIMITED_PARTNERSHIP_FIELD));
        assertTrue(filing.getData().containsKey(GENERAL_PARTNER_FIELD));
        assertTrue(filing.getData().containsKey(LIMITED_PARTNER_FIELD));
        assertTrue(filing.getData().containsKey(FILING_PAYMENT_METHOD));
        assertTrue(filing.getData().containsKey(FILING_PAYMENT_REFERENCE));
        assertEquals("Register a Limited Partnership", filing.getDescription());
    }

    @Test
    void testFilingDescriptionSetCorrectlyForTransition() throws ServiceException {
        var transaction = new TransactionBuilder().withIncorporationKind(FilingMode.TRANSITION).build();

        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(new LimitedPartnershipBuilder().buildDto());
        when(generalPartnerService.getGeneralPartnerDataList(transaction)).thenReturn(new ArrayList<>());
        when(limitedPartnerService.getLimitedPartnerDataList(transaction)).thenReturn(new ArrayList<>());
        FilingApi filing = filingsService.generateIncorporationFiling(transaction, INCORPORATION_ID);
        assertNotNull(filing);
        assertEquals("Transition a Limited Partnership", filing.getDescription());
    }

    @Test
    void testFilingGenerationFailureWhenTransactionNotLinkedToIncorporation() {
        var transaction = new TransactionBuilder().build();

        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> filingsService.generateIncorporationFiling(transaction, INCORPORATION_ID));
    }

    @Test
    void testFilingGenerationFailureWhenLimitedPartnershipNotFound() throws ServiceException {
        var transaction = new TransactionBuilder().build();

        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenThrow(ServiceException.class);
        assertThrows(ServiceException.class, () -> filingsService.generateIncorporationFiling(transaction, INCORPORATION_ID));
    }

    @Nested
    class FilingGeneralPartnerTest {
        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#add-general-partner-person",
                "false, limited-partnership#add-general-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForAddingGeneralPartner(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            var transaction = new TransactionBuilder().build();
            var generalPartnerBuilder = new GeneralPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withGeneralPartnerKind(partnerKind);

            var generalPartner = isPerson ? generalPartnerBuilder.personDto() : generalPartnerBuilder.legalEntityDto();

            when(generalPartnerService.getGeneralPartner(transaction, GENERAL_PARTNER_ID)).thenReturn(generalPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(generalPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateGeneralPartnerFiling(transaction, GENERAL_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(generalPartner.getData(), filingLimitedPartnershipData, transaction);

            List<GeneralPartnerDataDto> generalPartners = (List<GeneralPartnerDataDto>) filing.getData().get(GENERAL_PARTNER_FIELD);
            GeneralPartnerDataDto filingGeneralPartnerDataDto = generalPartners.getFirst();
            GeneralPartnerDataDto generalPartnerData = generalPartner.getData();

            assertCommonPartnerData(generalPartnerData, filingGeneralPartnerDataDto, isPerson);

            assertEquals(generalPartnerData.getServiceAddress(), filingGeneralPartnerDataDto.getServiceAddress());
            assertEquals(generalPartnerData.getUsualResidentialAddress(), filingGeneralPartnerDataDto.getUsualResidentialAddress());
        }

        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#update-general-partner-person",
                "false, limited-partnership#update-general-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForUpdatingGeneralPartnerWithAddresses(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            mockChsAppointmentApiData(isPerson);
            var transaction = new TransactionBuilder().build();

            var generalPartnerBuilder = new GeneralPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withGeneralPartnerKind(partnerKind)
                    .withUpdateServiceAddressRequired(Boolean.TRUE);

            if (isPerson) {
                generalPartnerBuilder.withUpdateUsualResidentialAddressRequired(Boolean.TRUE);
            } else {
                generalPartnerBuilder.withUpdatePrincipalOfficeAddressRequired(Boolean.TRUE);
            }

            var generalPartner = isPerson ? generalPartnerBuilder.personDto() : generalPartnerBuilder.legalEntityDto();

            when(generalPartnerService.getGeneralPartner(transaction, GENERAL_PARTNER_ID)).thenReturn(generalPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(generalPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateGeneralPartnerFiling(transaction, GENERAL_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(generalPartner.getData(), filingLimitedPartnershipData, transaction);

            List<GeneralPartnerDataDto> generalPartners = (List<GeneralPartnerDataDto>) filing.getData().get(GENERAL_PARTNER_FIELD);
            GeneralPartnerDataDto filingGeneralPartnerDataDto = generalPartners.getFirst();
            GeneralPartnerDataDto generalPartnerData = generalPartner.getData();

            assertCommonPartnerData(generalPartnerData, filingGeneralPartnerDataDto, isPerson);

            if (isPerson) {
                assertEquals("Prev forename", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getForename());
                assertEquals("Prev surname", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getSurname());
                assertEquals(LocalDate.of(1980, 6, 15), filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getDateOfBirth());
                assertEquals(generalPartnerData.getUsualResidentialAddress(), filingGeneralPartnerDataDto.getUsualResidentialAddress());
            } else {
                assertEquals("Prev legal entity name", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getLegalEntityName());
                assertEquals(generalPartnerData.getPrincipalOfficeAddress(), filingGeneralPartnerDataDto.getPrincipalOfficeAddress());
            }

            assertEquals(generalPartnerData.getServiceAddress(), filingGeneralPartnerDataDto.getServiceAddress());
        }

        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#update-general-partner-person",
                "false, limited-partnership#update-general-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForUpdatingGeneralPartnerWithoutAddresses(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            mockChsAppointmentApiData(isPerson);
            var transaction = new TransactionBuilder().build();
            var generalPartnerBuilder = new GeneralPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withGeneralPartnerKind(partnerKind)
                    .withUpdateUsualResidentialAddressRequired(Boolean.FALSE)
                    .withUpdatePrincipalOfficeAddressRequired(Boolean.FALSE)
                    .withUpdateServiceAddressRequired(Boolean.FALSE);

            var generalPartner = isPerson ? generalPartnerBuilder.personDto() : generalPartnerBuilder.legalEntityDto();

            when(generalPartnerService.getGeneralPartner(transaction, GENERAL_PARTNER_ID)).thenReturn(generalPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(generalPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateGeneralPartnerFiling(transaction, GENERAL_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(generalPartner.getData(), filingLimitedPartnershipData, transaction);

            List<GeneralPartnerDataDto> generalPartners = (List<GeneralPartnerDataDto>) filing.getData().get(GENERAL_PARTNER_FIELD);
            GeneralPartnerDataDto filingGeneralPartnerDataDto = generalPartners.getFirst();
            GeneralPartnerDataDto generalPartnerData = generalPartner.getData();

            assertCommonPartnerData(generalPartnerData, filingGeneralPartnerDataDto, isPerson);

            assertNull(filingGeneralPartnerDataDto.getUsualResidentialAddress());
            assertNull(filingGeneralPartnerDataDto.getPrincipalOfficeAddress());
            assertNull(filingGeneralPartnerDataDto.getServiceAddress());

            if (isPerson) {
                assertEquals("Prev forename", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getForename());
                assertEquals("Prev surname", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getSurname());
                assertEquals(LocalDate.of(1980, 6, 15), filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getDateOfBirth());
            } else {
                assertEquals("Prev legal entity name", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getLegalEntityName());
            }
        }

        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#remove-general-partner-person",
                "false, limited-partnership#remove-general-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForRemovingGeneralPartnerPerson(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            mockChsAppointmentApiData(isPerson);
            var transaction = new TransactionBuilder().build();
            LocalDate today = LocalDate.now();
            var generalPartner = new GeneralPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withGeneralPartnerKind(partnerKind)
                    .withCeaseDate(today)
                    .personDto();

            when(generalPartnerService.getGeneralPartner(transaction, GENERAL_PARTNER_ID)).thenReturn(generalPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(generalPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateGeneralPartnerFiling(transaction, GENERAL_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(generalPartner.getData(), filingLimitedPartnershipData, transaction);

            List<GeneralPartnerDataDto> generalPartners = (List<GeneralPartnerDataDto>) filing.getData().get(GENERAL_PARTNER_FIELD);
            GeneralPartnerDataDto filingGeneralPartnerDataDto = generalPartners.getFirst();
            GeneralPartnerDataDto generalPartnerData = generalPartner.getData();

            assertCommonPartnerData(generalPartnerData, filingGeneralPartnerDataDto, isPerson);

            if (isPerson) {
                assertEquals("Prev forename", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getForename());
                assertEquals("Prev surname", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getSurname());
                assertEquals(LocalDate.of(1980, 6, 15), filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getDateOfBirth());
            } else {
                assertEquals("Prev legal entity name", filingGeneralPartnerDataDto.getAppointmentPreviousDetails().getLegalEntityName());
            }

            assertEquals(today, filingGeneralPartnerDataDto.getCeaseDate());
        }
    }

    @Nested
    class FilingLimitedPartnerTest {
        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#add-limited-partner-person",
                "false, limited-partnership#add-limited-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForAddingLimitedPartner(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            var transaction = new TransactionBuilder().build();
            var limitedPartnerBuilder = new LimitedPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withLimitedPartnerKind(partnerKind);

            var limitedPartner = isPerson ? limitedPartnerBuilder.personDto() : limitedPartnerBuilder.legalEntityDto();

            when(limitedPartnerService.getLimitedPartner(transaction, LIMITED_PARTNER_ID)).thenReturn(limitedPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(limitedPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateLimitedPartnerFiling(transaction, LIMITED_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(limitedPartner.getData(), filingLimitedPartnershipData, transaction);

            List<LimitedPartnerDataDto> limitedPartners = (List<LimitedPartnerDataDto>) filing.getData().get(LIMITED_PARTNER_FIELD);
            LimitedPartnerDataDto filingLimitedPartnerDataDto = limitedPartners.getFirst();
            LimitedPartnerDataDto limitedPartnerData = limitedPartner.getData();

            assertCommonPartnerData(limitedPartnerData, filingLimitedPartnerDataDto, isPerson);

            assertEquals(limitedPartnerData.getContributionSubTypes(), filingLimitedPartnerDataDto.getContributionSubTypes());
            assertEquals(limitedPartnerData.getContributionCurrencyType(), filingLimitedPartnerDataDto.getContributionCurrencyType());
            assertEquals(limitedPartnerData.getContributionCurrencyValue(), filingLimitedPartnerDataDto.getContributionCurrencyValue());

            assertEquals(limitedPartnerData.getUsualResidentialAddress(), filingLimitedPartnerDataDto.getUsualResidentialAddress());

        }

        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#update-limited-partner-person",
                "false, limited-partnership#update-limited-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForUpdatingLimitedPartnerWithAddresses(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            mockChsAppointmentApiData(isPerson);
            var transaction = new TransactionBuilder().build();
            var limitedPartnerBuilder = new LimitedPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withLimitedPartnerKind(partnerKind)
                    .withUpdateServiceAddressRequired(Boolean.TRUE);

            if (isPerson) {
                limitedPartnerBuilder.withUpdateUsualResidentialAddressRequired(Boolean.TRUE);
            } else {
                limitedPartnerBuilder.withUpdatePrincipalOfficeAddressRequired(Boolean.TRUE);
            }

            var limitedPartner = isPerson ? limitedPartnerBuilder.personDto() : limitedPartnerBuilder.legalEntityDto();

            when(limitedPartnerService.getLimitedPartner(transaction, LIMITED_PARTNER_ID)).thenReturn(limitedPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(limitedPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateLimitedPartnerFiling(transaction, LIMITED_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(limitedPartner.getData(), filingLimitedPartnershipData, transaction);

            List<LimitedPartnerDataDto> limitedPartners = (List<LimitedPartnerDataDto>) filing.getData().get(LIMITED_PARTNER_FIELD);
            LimitedPartnerDataDto filingLimitedPartnerDataDto = limitedPartners.getFirst();
            LimitedPartnerDataDto limitedPartnerData = limitedPartner.getData();

            assertCommonPartnerData(limitedPartnerData, filingLimitedPartnerDataDto, isPerson);

            assertEquals(limitedPartnerData.getUsualResidentialAddress(), filingLimitedPartnerDataDto.getUsualResidentialAddress());
            assertEquals(limitedPartnerData.getServiceAddress(), filingLimitedPartnerDataDto.getServiceAddress());

            if (isPerson) {
                assertEquals("Prev forename", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getForename());
                assertEquals("Prev surname", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getSurname());
                assertEquals(LocalDate.of(1980, 6, 15), filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getDateOfBirth());
                assertEquals(limitedPartnerData.getUsualResidentialAddress(), filingLimitedPartnerDataDto.getUsualResidentialAddress());
            } else {
                assertEquals("Prev legal entity name", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getLegalEntityName());
                assertEquals(limitedPartnerData.getPrincipalOfficeAddress(), filingLimitedPartnerDataDto.getPrincipalOfficeAddress());
            }

            assertEquals(limitedPartnerData.getServiceAddress(), filingLimitedPartnerDataDto.getServiceAddress());
        }

        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#update-limited-partner-person",
                "false, limited-partnership#update-limited-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForUpdatingLimitedPartnerWithoutAddresses(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            mockChsAppointmentApiData(isPerson);
            var transaction = new TransactionBuilder().build();
            var limitedPartnerBuilder = new LimitedPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withLimitedPartnerKind(partnerKind)
                    .withUpdateUsualResidentialAddressRequired(Boolean.FALSE)
                    .withUpdatePrincipalOfficeAddressRequired(Boolean.FALSE)
                    .withUpdateServiceAddressRequired(Boolean.FALSE);

            var limitedPartner = isPerson ? limitedPartnerBuilder.personDto() : limitedPartnerBuilder.legalEntityDto();

            when(limitedPartnerService.getLimitedPartner(transaction, LIMITED_PARTNER_ID)).thenReturn(limitedPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(limitedPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateLimitedPartnerFiling(transaction, LIMITED_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(limitedPartner.getData(), filingLimitedPartnershipData, transaction);

            List<LimitedPartnerDataDto> limitedPartners = (List<LimitedPartnerDataDto>) filing.getData().get(LIMITED_PARTNER_FIELD);
            LimitedPartnerDataDto filingLimitedPartnerDataDto = limitedPartners.getFirst();
            LimitedPartnerDataDto limitedPartnerData = limitedPartner.getData();

            assertCommonPartnerData(limitedPartnerData, filingLimitedPartnerDataDto, isPerson);

            assertNull(filingLimitedPartnerDataDto.getUsualResidentialAddress());
            assertNull(filingLimitedPartnerDataDto.getServiceAddress());

            if (isPerson) {
                assertEquals("Prev forename", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getForename());
                assertEquals("Prev surname", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getSurname());
                assertEquals(LocalDate.of(1980, 6, 15), filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getDateOfBirth());
                assertEquals(limitedPartnerData.getUsualResidentialAddress(), filingLimitedPartnerDataDto.getUsualResidentialAddress());
            } else {
                assertEquals("Prev legal entity name", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getLegalEntityName());
                assertEquals(limitedPartnerData.getPrincipalOfficeAddress(), filingLimitedPartnerDataDto.getPrincipalOfficeAddress());
            }

            assertEquals(limitedPartnerData.getServiceAddress(), filingLimitedPartnerDataDto.getServiceAddress());
        }

        @ParameterizedTest
        @CsvSource({
                "true, limited-partnership#remove-limited-partner-person",
                "false, limited-partnership#remove-limited-partner-legal-entity"
        })
        void testFilingGenerationSuccessfulForRemovingLimitedPartnerPerson(
                boolean isPerson,
                String partnerKind) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
            mockChsAppointmentApiData(isPerson);
            var transaction = new TransactionBuilder().build();
            LocalDate today = LocalDate.now();
            var limitedPartner = new LimitedPartnerBuilder()
                    .withPartnershipType(PartnershipType.LP)
                    .withLimitedPartnerKind(partnerKind)
                    .withCeaseDate(today)
                    .personDto();

            when(limitedPartnerService.getLimitedPartner(transaction, LIMITED_PARTNER_ID)).thenReturn(limitedPartner);
            when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(limitedPartner.getData().getKind()))).thenReturn(true);

            FilingApi filing = filingsService.generateLimitedPartnerFiling(transaction, LIMITED_PARTNER_ID);

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertCommonLimitedPartnershipData(limitedPartner.getData(), filingLimitedPartnershipData, transaction);

            List<LimitedPartnerDataDto> limitedPartners = (List<LimitedPartnerDataDto>) filing.getData().get(LIMITED_PARTNER_FIELD);
            LimitedPartnerDataDto filingLimitedPartnerDataDto = limitedPartners.getFirst();
            LimitedPartnerDataDto limitedPartnerData = limitedPartner.getData();

            assertCommonPartnerData(limitedPartnerData, filingLimitedPartnerDataDto, isPerson);

            if (isPerson) {
                assertEquals("Prev forename", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getForename());
                assertEquals("Prev surname", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getSurname());
                assertEquals(LocalDate.of(1980, 6, 15), filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getDateOfBirth());
            } else {
                assertEquals("Prev legal entity name", filingLimitedPartnerDataDto.getAppointmentPreviousDetails().getLegalEntityName());
            }

            assertEquals(today, filingLimitedPartnerDataDto.getCeaseDate());
        }
    }

    @Nested
    class FilingLimitedPartnershipTest {
        @Test
        void testFilingGenerationSuccessfulForUpdateLimitedPartnershipName() throws ServiceException {
            String currentCompanyName = "Current Company Name Ltd";
            LocalDate today = LocalDate.now();
            var transaction = new TransactionBuilder()
                    .withFilingMode(FilingMode.DEFAULT.getDescription())
                    .build();

            var limitedPartnership = new LimitedPartnershipBuilder()
                    .withPartnershipKind(PartnershipKind.UPDATE_PARTNERSHIP_NAME)
                    .withDateOfUpdate(today)
                    .buildDto();

            var companyProfile = new CompanyProfileApi();
            companyProfile.setCompanyName(currentCompanyName);

            PaymentApi payment = new PaymentApi();
            payment.setPaymentMethod(PAYMENT_METHOD);

            when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(limitedPartnership);
            when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfile);
            when(transactionService.getPaymentReference(transaction.getLinks().getPayment())).thenReturn(PAYMENT_REFERENCE);
            when(paymentService.getPayment(PAYMENT_REFERENCE)).thenReturn(payment);

            FilingApi filing = filingsService.generateLimitedPartnershipFiling(transaction);
            var expectedFilingKind = new FilingKind().addSubKind(FilingMode.POST_TRANSITION.getDescription(), PartnershipKind.UPDATE_PARTNERSHIP_NAME.getDescription());
            assertEquals(expectedFilingKind, filing.getKind());

            DataDto filingLimitedPartnershipData = (DataDto) filing.getData().get(LIMITED_PARTNERSHIP_FIELD);
            assertEquals(currentCompanyName, filingLimitedPartnershipData.getCompanyPreviousDetails().getCompanyName());
            assertEquals(limitedPartnership.getData().getPartnershipName(), filingLimitedPartnershipData.getPartnershipName());
            assertEquals(limitedPartnership.getData().getPartnershipNumber(), filingLimitedPartnershipData.getPartnershipNumber());
            assertEquals(limitedPartnership.getData().getPartnershipType(), filingLimitedPartnershipData.getPartnershipType());
            assertEquals(limitedPartnership.getData().getDateOfUpdate(), filingLimitedPartnershipData.getDateOfUpdate());
        }
    }

    private static void assertCommonLimitedPartnershipData(PartnerDataDto partnerDataDto, DataDto filingLimitedPartnershipData, Transaction transaction) {
        assertEquals(partnerDataDto.getPartnershipType(), filingLimitedPartnershipData.getPartnershipType());
        assertEquals(COMPANY_NUMBER, transaction.getCompanyNumber());
    }

    private static void assertCommonPartnerData(PartnerDataDto partnerData, PartnerDataDto filingPartnerDataDto, boolean isPerson) {
        if (isPerson) {
            assertEquals(partnerData.getForename(), filingPartnerDataDto.getForename());
            assertEquals(partnerData.getSurname(), filingPartnerDataDto.getSurname());
        } else {
            assertEquals(partnerData.getLegalEntityName(), filingPartnerDataDto.getLegalEntityName());
        }

        assertEquals(partnerData.getDateOfBirth(), filingPartnerDataDto.getDateOfBirth());
        assertEquals(partnerData.getDateEffectiveFrom(), filingPartnerDataDto.getDateEffectiveFrom());
        assertEquals(partnerData.getUsualResidentialAddress(), filingPartnerDataDto.getUsualResidentialAddress());
        assertEquals(partnerData.getPrincipalOfficeAddress(), filingPartnerDataDto.getPrincipalOfficeAddress());
        assertEquals(partnerData.getKind(), filingPartnerDataDto.getKind());
    }

    private void mockChsAppointmentApiData(boolean isPerson) throws URIValidationException, ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.getAppointment(any())).thenReturn(privateOfficerGet);
        when(privateOfficerGet.execute()).thenReturn(appointmentFullRecordAPIApiResponse);
        when(appointmentFullRecordAPIApiResponse.getData()).thenReturn(getAppointmentFullRecordAPI(isPerson));
    }

    private AppointmentFullRecordAPI getAppointmentFullRecordAPI(boolean isPerson) {
        AppointmentFullRecordAPI appointmentFullRecordAPI = new AppointmentFullRecordAPI();
        SensitiveDateOfBirthAPI sensitiveDateOfBirthAPI = new SensitiveDateOfBirthAPI();
        sensitiveDateOfBirthAPI.setDay(15);
        sensitiveDateOfBirthAPI.setMonth(6);
        sensitiveDateOfBirthAPI.setYear(1980);
        appointmentFullRecordAPI.setDateOfBirth(sensitiveDateOfBirthAPI);

        if (isPerson) {
            appointmentFullRecordAPI.setForename("Prev forename");
            appointmentFullRecordAPI.setSurname("Prev surname");
        } else {
            appointmentFullRecordAPI.setSurname("Prev legal entity name");
        }

        return appointmentFullRecordAPI;
    }
}
