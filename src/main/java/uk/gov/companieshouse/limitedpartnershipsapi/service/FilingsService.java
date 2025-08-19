package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.FilingKind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.GENERAL_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNER_FIELD;
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
    private final FilingKind filingKind;

    public FilingsService(LimitedPartnershipService limitedPartnershipService,
                          GeneralPartnerService generalPartnerService,
                          LimitedPartnerService limitedPartnerService,
                          TransactionService transactionService,
                          FilingKind filingKind
    ) {

        this.limitedPartnershipService = limitedPartnershipService;
        this.generalPartnerService = generalPartnerService;
        this.limitedPartnerService = limitedPartnerService;
        this.transactionService = transactionService;
        this.filingKind = filingKind;
    }

    public FilingApi generateLimitedPartnershipFiling(Transaction transaction, String incorporationId) throws ServiceException {
        String submissionUri = String.format(URL_GET_INCORPORATION, transaction.getId(), incorporationId);
        if (!transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches incorporation id: %s", transaction.getId(), incorporationId));
        }

        var filing = new FilingApi();
        setFilingApiData(filing, transaction);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, Transaction transaction) throws ServiceException {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_KEY, transaction.getId());

        Map<String, Object> data = new HashMap<>();

        var limitedPartnershipDto = limitedPartnershipService.getLimitedPartnership(transaction);
        List<GeneralPartnerDataDto> generalPartnersDataList = generalPartnerService.getGeneralPartnerDataList(transaction);
        List<LimitedPartnerDataDto> limitedPartnerDataList = limitedPartnerService.getLimitedPartnerDataList(transaction);
        setSubmissionData(data, limitedPartnershipDto, generalPartnersDataList, limitedPartnerDataList, logMap);
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
        if (transactionFilingMode.equals(IncorporationKind.REGISTRATION.getDescription())) {
            filing.setDescription(LIMITED_PARTNERSHIP_REGISTRATION_FILING_DESCRIPTION);
        } else if (transactionFilingMode.equals(IncorporationKind.TRANSITION.getDescription())) {
            filing.setDescription(LIMITED_PARTNERSHIP_TRANSITION_FILING_DESCRIPTION);
        } else {
            filing.setDescription(LIMITED_PARTNERSHIP_POST_TRANSITION_FILING_DESCRIPTION);
        }
        filing.setDescriptionValues(new HashMap<>());
    }

    public FilingApi generateGeneralPartnerFiling(Transaction transaction, String generalPartnerId) throws ResourceNotFoundException {
        GeneralPartnerDto generalPartnerDto = generalPartnerService.getGeneralPartner(transaction, generalPartnerId);

        GeneralPartnerDataDto generalPartnerDataDto = generalPartnerDto.getData();

        String submissionUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), generalPartnerId);
        if (!transactionService.isTransactionLinkedToPartner(transaction, submissionUri, generalPartnerDataDto.getKind())) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches General Partner id: %s", transaction.getId(), generalPartnerId));
        }

        var filing = new FilingApi();

        Map<String, Object> data = new HashMap<>();

        data.put(LIMITED_PARTNERSHIP_FIELD, buildLimitedPartnershipDataWithPartnershipNumber(transaction));
        data.put(GENERAL_PARTNER_FIELD, List.of(generalPartnerDataDto));

        String kind = filingKind.addSubKind(IncorporationKind.POST_TRANSITION.getDescription(), generalPartnerDataDto.getKind());
        filing.setKind(kind);
        setDescriptionFields(filing, transaction.getFilingMode());
        filing.setData(data);

        return filing;
    }

    public FilingApi generateLimitedPartnerFiling(Transaction transaction, String limitedPartnerId) throws ResourceNotFoundException {
        LimitedPartnerDto limitedPartnerDto = limitedPartnerService.getLimitedPartner(transaction, limitedPartnerId);

        LimitedPartnerDataDto limitedPartnerDataDto = limitedPartnerDto.getData();

        String submissionUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), limitedPartnerId);
        if (!transactionService.isTransactionLinkedToPartner(transaction, submissionUri, limitedPartnerDataDto.getKind())) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches Limited Partner id: %s", transaction.getId(), limitedPartnerId));
        }

        var filing = new FilingApi();

        Map<String, Object> data = new HashMap<>();

        data.put(LIMITED_PARTNER_FIELD, List.of(limitedPartnerDataDto));

        String kind = filingKind.addSubKind(IncorporationKind.POST_TRANSITION.getDescription(), limitedPartnerDataDto.getKind());
        filing.setKind(kind);
        setDescriptionFields(filing, transaction.getFilingMode());
        filing.setData(data);

        return filing;
    }

    private DataDto buildLimitedPartnershipDataWithPartnershipNumber(Transaction transaction) {
        DataDto dataDto = new DataDto();
        dataDto.setPartnershipNumber(transaction.getCompanyNumber());
        return dataDto;
    }
}
