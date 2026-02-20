package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AppointmentPreviousDetailsDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.CompanyPreviousDetailsDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.FilingKind;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_PAYMENT_METHOD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_PAYMENT_REFERENCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.GENERAL_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_COSTS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@Service
public class FilingsService {

    public static final String LIMITED_PARTNERSHIP_REGISTRATION_FILING_DESCRIPTION = "Register a Limited Partnership";
    public static final String LIMITED_PARTNERSHIP_TRANSITION_FILING_DESCRIPTION = "Transition a Limited Partnership";
    public static final String LIMITED_PARTNERSHIP_POST_TRANSITION_FILING_DESCRIPTION = "Post Transition a Limited Partnership";

    private final LimitedPartnershipService limitedPartnershipService;
    private final GeneralPartnerService generalPartnerService;
    private final LimitedPartnerService limitedPartnerService;
    private final TransactionService transactionService;
    private final PaymentService paymentService;
    private final ApiClientService apiClientService;
    private final CompanyService companyService;
    private final FilingKind filingKind;

    public FilingsService(LimitedPartnershipService limitedPartnershipService,
                          GeneralPartnerService generalPartnerService,
                          LimitedPartnerService limitedPartnerService,
                          TransactionService transactionService,
                          PaymentService paymentService,
                          FilingKind filingKind,
                          ApiClientService apiClientService,
                          CompanyService companyService
    ) {

        this.limitedPartnershipService = limitedPartnershipService;
        this.generalPartnerService = generalPartnerService;
        this.limitedPartnerService = limitedPartnerService;
        this.transactionService = transactionService;
        this.paymentService = paymentService;
        this.filingKind = filingKind;
        this.apiClientService = apiClientService;
        this.companyService = companyService;
    }

    public FilingApi generateIncorporationFiling(Transaction transaction, String incorporationId) throws ServiceException {
        String submissionUri = String.format(URL_GET_INCORPORATION, transaction.getId(), incorporationId);
        if (!transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches incorporation id: %s", transaction.getId(), incorporationId));
        }

        var filing = new FilingApi();
        setFilingApiData(filing, transaction);

        addCostLinkInFilingIsExistsInResource(transaction, filing);

        return filing;
    }

    private void setFilingApiData(FilingApi filing, Transaction transaction) throws ServiceException {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());

        Map<String, Object> data = new HashMap<>();

        var limitedPartnershipDto = limitedPartnershipService.getLimitedPartnership(transaction);
        List<GeneralPartnerDataDto> generalPartnerDataList = generalPartnerService.getGeneralPartnerDataList(transaction);
        List<LimitedPartnerDataDto> limitedPartnerDataList = limitedPartnerService.getLimitedPartnerDataList(transaction);

        setSubmissionData(data, limitedPartnershipDto, generalPartnerDataList, limitedPartnerDataList, logMap);
        setPaymentData(data, transaction);
        filing.setData(data);
        filing.setKind(transaction.getFilingMode());
        setDescriptionFields(filing, transaction.getFilingMode());
    }

    private void setSubmissionData(Map<String, Object> data,
                                   LimitedPartnershipDto limitedPartnershipDto,
                                   List<GeneralPartnerDataDto> generalPartnersDataList,
                                   List<LimitedPartnerDataDto> limitedPartnersDataList,
                                   Map<String, Object> logMap) {

        data.put(LIMITED_PARTNERSHIP_FIELD, limitedPartnershipDto.getData());
        data.put(GENERAL_PARTNER_FIELD, generalPartnersDataList);
        data.put(LIMITED_PARTNER_FIELD, limitedPartnersDataList);
        ApiLogger.info("Submission data has been set on filing", logMap);
    }

    private void setDescriptionFields(FilingApi filing, String transactionFilingMode) {
        if (transactionFilingMode.equals(FilingMode.REGISTRATION.getDescription())) {
            filing.setDescription(LIMITED_PARTNERSHIP_REGISTRATION_FILING_DESCRIPTION);
        } else if (transactionFilingMode.equals(FilingMode.TRANSITION.getDescription())) {
            filing.setDescription(LIMITED_PARTNERSHIP_TRANSITION_FILING_DESCRIPTION);
        } else {
            filing.setDescription(LIMITED_PARTNERSHIP_POST_TRANSITION_FILING_DESCRIPTION);
        }
        filing.setDescriptionValues(new HashMap<>());
    }

    public FilingApi generateGeneralPartnerFiling(Transaction transaction, String generalPartnerId) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
        GeneralPartnerDto generalPartnerDto = generalPartnerService.getGeneralPartner(transaction, generalPartnerId);
        GeneralPartnerDataDto generalPartnerDataDto = generalPartnerDto.getData();

        updatePartnerAddresses(generalPartnerDataDto);
        setExtraData(generalPartnerDataDto, transaction);

        String submissionUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), generalPartnerId);
        if (!transactionService.isTransactionLinkedToPartner(transaction, submissionUri, generalPartnerDataDto.getKind())) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches General Partner id: %s", transaction.getId(), generalPartnerId));
        }

        var filing = new FilingApi();

        Map<String, Object> data = new HashMap<>();

        data.put(LIMITED_PARTNERSHIP_FIELD, buildLimitedPartnershipData(transaction, generalPartnerDataDto));
        data.put(GENERAL_PARTNER_FIELD, List.of(generalPartnerDataDto));

        String kind = filingKind.addSubKind(FilingMode.POST_TRANSITION.getDescription(), generalPartnerDataDto.getKind());
        filing.setKind(kind);
        setDescriptionFields(filing, transaction.getFilingMode());
        filing.setData(data);

        return filing;
    }

    public FilingApi generateLimitedPartnerFiling(Transaction transaction, String limitedPartnerId) throws ResourceNotFoundException, ApiErrorResponseException, URIValidationException {
        LimitedPartnerDto limitedPartnerDto = limitedPartnerService.getLimitedPartner(transaction, limitedPartnerId);
        LimitedPartnerDataDto limitedPartnerDataDto = limitedPartnerDto.getData();

        updatePartnerAddresses(limitedPartnerDataDto);
        setExtraData(limitedPartnerDataDto, transaction);

        String submissionUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), limitedPartnerId);
        if (!transactionService.isTransactionLinkedToPartner(transaction, submissionUri, limitedPartnerDataDto.getKind())) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches Limited Partner id: %s", transaction.getId(), limitedPartnerId));
        }

        var filing = new FilingApi();

        Map<String, Object> data = new HashMap<>();

        data.put(LIMITED_PARTNERSHIP_FIELD, buildLimitedPartnershipData(transaction, limitedPartnerDataDto));
        data.put(LIMITED_PARTNER_FIELD, List.of(limitedPartnerDataDto));

        String kind = filingKind.addSubKind(FilingMode.POST_TRANSITION.getDescription(), limitedPartnerDataDto.getKind());
        filing.setKind(kind);
        setDescriptionFields(filing, transaction.getFilingMode());
        filing.setData(data);

        return filing;
    }

    private void updatePartnerAddresses(PartnerDataDto partnerDataDto) {
        if (PartnerKind.isUpdatePartnerKind(partnerDataDto.getKind())) {
            if (partnerDataDto.getUpdateUsualResidentialAddressRequired() == Boolean.FALSE) {
                partnerDataDto.setUsualResidentialAddress(null);
            }

            if (partnerDataDto.getUpdatePrincipalOfficeAddressRequired() == Boolean.FALSE) {
                partnerDataDto.setPrincipalOfficeAddress(null);
            }

            if (partnerDataDto.getUpdateServiceAddressRequired() == Boolean.FALSE) {
                partnerDataDto.setServiceAddress(null);
            }
        }
    }

    private void setExtraData(PartnerDataDto partnerDataDto, Transaction transaction) throws URIValidationException, ApiErrorResponseException {
        String partnerKind = partnerDataDto.getKind();

        if (!PartnerKind.isUpdatePartnerKind(partnerKind) &&
                !PartnerKind.isRemovePartnerKind(partnerKind)) {
            return;
        }

        String companyNumber = transaction.getCompanyNumber();
        String appointmentId = partnerDataDto.getAppointmentId();

        String uri = String.format("/company/%s/appointments/%s/full_record", companyNumber, appointmentId);
        ApiResponse<AppointmentFullRecordAPI> response = apiClientService.getInternalApiClient().privateDeltaResourceHandler().getAppointment(uri).execute();
        AppointmentFullRecordAPI appointmentFullRecordAPI = response.getData();

        setSensitiveData(partnerDataDto, appointmentFullRecordAPI);

        AppointmentPreviousDetailsDto appointmentPreviousDetails = new AppointmentPreviousDetailsDto();

        if (partnerKind.contains("person")) {
            appointmentPreviousDetails.setForename(appointmentFullRecordAPI.getForename());
            appointmentPreviousDetails.setSurname(appointmentFullRecordAPI.getSurname());
            appointmentPreviousDetails.setDateOfBirth(LocalDate.of(
                    appointmentFullRecordAPI.getDateOfBirth().getYear(),
                    appointmentFullRecordAPI.getDateOfBirth().getMonth(),
                    appointmentFullRecordAPI.getDateOfBirth().getDay()));
        } else {
            appointmentPreviousDetails.setLegalEntityName(appointmentFullRecordAPI.getSurname());
        }

        partnerDataDto.setAppointmentPreviousDetails(appointmentPreviousDetails);
    }

    private void setSensitiveData(PartnerDataDto partnerDataDto, AppointmentFullRecordAPI appointmentFullRecordAPI) {
        if (appointmentFullRecordAPI.getDateOfBirth() != null) {
            partnerDataDto.setDateOfBirth(LocalDate.of(
                    appointmentFullRecordAPI.getDateOfBirth().getYear(),
                    appointmentFullRecordAPI.getDateOfBirth().getMonth(),
                    appointmentFullRecordAPI.getDateOfBirth().getDay()));
        }
    }

    private DataDto buildLimitedPartnershipData(Transaction transaction, PartnerDataDto partnerDataDto) {
        DataDto dataDto = new DataDto();
        dataDto.setPartnershipNumber(transaction.getCompanyNumber());
        dataDto.setPartnershipType(partnerDataDto.getPartnershipType());
        return dataDto;
    }

    public FilingApi generateLimitedPartnershipFiling(Transaction transaction) throws ServiceException {
        LimitedPartnershipDto limitedPartnershipDto = limitedPartnershipService.getLimitedPartnership(transaction);
        DataDto limitedPartnershipDataDto = limitedPartnershipDto.getData();

        if (shouldIncludeCompanyPreviousDetails(transaction, limitedPartnershipDataDto)) {
            CompanyProfileApi companyProfile = companyService.getCompanyProfile(transaction.getCompanyNumber());
            CompanyPreviousDetailsDto companyPreviousDetailsDto = new CompanyPreviousDetailsDto();
            companyPreviousDetailsDto.setCompanyName(companyProfile.getCompanyName());
            limitedPartnershipDataDto.setCompanyPreviousDetails(companyPreviousDetailsDto);
        }

        var filing = new FilingApi();

        Map<String, Object> data = new HashMap<>();

        data.put(LIMITED_PARTNERSHIP_FIELD, limitedPartnershipDataDto);

        String kind = filingKind.addSubKind(FilingMode.POST_TRANSITION.getDescription(), limitedPartnershipDataDto.getKind());
        filing.setKind(kind);
        setDescriptionFields(filing, transaction.getFilingMode());
        setPaymentData(data, transaction);
        filing.setData(data);

        addCostLinkInFilingIsExistsInResource(transaction, filing);

        return filing;
    }

    private static void addCostLinkInFilingIsExistsInResource(Transaction transaction, FilingApi filing) {
        Resource resource = transaction.getResources().values().stream().toList().getFirst();
        String cost = resource.getLinks().get(LINK_COSTS);
        if (cost != null) {
            filing.setCost(cost);
        }
    }

    private boolean shouldIncludeCompanyPreviousDetails(Transaction transaction, DataDto limitedPartnershipDataDto) {
        return FilingMode.DEFAULT.getDescription().equals(transaction.getFilingMode()) && PartnershipKind.UPDATE_PARTNERSHIP_NAME.getDescription().equals(limitedPartnershipDataDto.getKind());
    }

    /**
     * Populates the provided data map with payment information for the given transaction.
     * <p>
     * If a payment link exists in the transaction, retrieves the payment reference and payment method,
     * and adds them to the data map. If no payment link is present or payment cannot be retrieved,
     * the method returns without modifying the map.
     *
     * @param data        the map to populate with payment data
     * @param transaction the transaction containing payment link information
     * @throws ServiceException if an error occurs while retrieving payment information
     */
    private void setPaymentData(Map<String, Object> data, Transaction transaction) throws ServiceException {
        if (transaction.getLinks() == null || !StringUtils.hasText(transaction.getLinks().getPayment())) {
            // Transaction has no payment link so no payment data to set
            return;
        }

        var paymentReference = transactionService.getPaymentReference(transaction.getLinks().getPayment());
        var payment = paymentService.getPayment(paymentReference);
        data.put(FILING_PAYMENT_REFERENCE, paymentReference);
        data.put(FILING_PAYMENT_METHOD, payment.getPaymentMethod());
    }
}
