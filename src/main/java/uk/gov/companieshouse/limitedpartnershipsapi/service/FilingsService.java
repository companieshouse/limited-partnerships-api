package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.GENERAL_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;

@Service
public class FilingsService {

    public static final String LIMITED_PARTNERSHIP_FILING_DESCRIPTION = "Register a Limited Partnership";

    private final LimitedPartnershipService limitedPartnershipService;
    private final GeneralPartnerService generalPartnerService;
    private final LimitedPartnerService limitedPartnerService;

    @Autowired
    public FilingsService(LimitedPartnershipService limitedPartnershipService,
                          GeneralPartnerService generalPartnerService,
                          LimitedPartnerService limitedPartnerService){

        this.limitedPartnershipService = limitedPartnershipService;
        this.generalPartnerService = generalPartnerService;
        this.limitedPartnerService = limitedPartnerService;
    }

    public FilingApi generateLimitedPartnerFiling(Transaction transaction) throws ServiceException {
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
        setDescriptionFields(filing);
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

    private void setDescriptionFields(FilingApi filing) {
        filing.setDescription(LIMITED_PARTNERSHIP_FILING_DESCRIPTION);
        filing.setDescriptionValues(new HashMap<>());
    }
}
