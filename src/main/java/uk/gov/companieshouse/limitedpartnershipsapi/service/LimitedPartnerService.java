package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@Service
public class LimitedPartnerService {

    private final LimitedPartnerRepository repository;
    private final TransactionService transactionService;
    private final LimitedPartnerMapper mapper;

    public LimitedPartnerService(
            LimitedPartnerRepository repository,
            TransactionService transactionService,
            LimitedPartnerMapper mapper) {
        this.repository = repository;
        this.transactionService = transactionService;
        this.mapper = mapper;
    }

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId) throws ServiceException {
        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnerDao insertedSubmission = repository.insert(dao);

        ApiLogger.infoContext(requestId, String.format("Limited Partner submission created with id: %s", insertedSubmission.getId()));

        transaction.setFilingMode(FILING_KIND_LIMITED_PARTNER);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedSubmission.getId());

        updateLimitedPartnerTypeWithSelfLink(dao, submissionUri);

        var limitedPartnerResource = createLimitedPartnerTransactionResource(submissionUri);

        updateTransactionWithLinksForLimitedPartner(transaction, limitedPartnerDto,
                submissionUri, limitedPartnerResource, requestId);

        return insertedSubmission.getId();
    }

    private void updateTransactionWithLinksForLimitedPartner(Transaction transaction,
                                                             LimitedPartnerDto limitedPartnerDto,
                                                             String submissionUri,
                                                             Resource limitedPartnerResource,
                                                             String loggingContext) throws ServiceException {
        transaction.setCompanyName(limitedPartnerDto.getData().getPartnerType().getDescription());

        // Retrieve existing resources
        Map<String, Resource> existingResources = transaction.getResources();
        if (existingResources == null) {
            existingResources = new HashMap<>();
        }

        existingResources.put(submissionUri, limitedPartnerResource);

        // Set the updated resources back to the transaction
        transaction.setResources(existingResources);

    }

    private void updateLimitedPartnerTypeWithSelfLink(LimitedPartnerDao limitedPartnerDao,
                                                      String submissionUri) {
        limitedPartnerDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(limitedPartnerDao);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId);
    }

    private Resource createLimitedPartnerTransactionResource(String submissionUri) {
        var limitedPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);

        // TODO Add 'validation status' and 'cost' links here later

        limitedPartnerResource.setLinks(linksMap);
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);

        return limitedPartnerResource;
    }
}
