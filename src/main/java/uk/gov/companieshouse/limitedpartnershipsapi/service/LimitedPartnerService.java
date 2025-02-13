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
    private final LimitedPartnerMapper mapper;
    private final TransactionService transactionService;

    public LimitedPartnerService(
            LimitedPartnerRepository repository,
            LimitedPartnerMapper mapper,
            TransactionService transactionService) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionService = transactionService;
    }

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId) throws ServiceException {
        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());

        LimitedPartnerDao insertedSubmission = repository.insert(dao);

        ApiLogger.infoContext(requestId, String.format("Limited Partner submission created with id: %s", insertedSubmission.getId()));

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedSubmission.getId());

        updateLimitedPartnerTypeWithSelfLink(dao, submissionUri);

        var limitedPartnerResource = createLimitedPartnerTransactionResource(submissionUri);

        updateTransactionWithLinksForLimitedPartner(transaction,
                submissionUri, limitedPartnerResource, requestId);

        return insertedSubmission.getId();
    }

    private void updateTransactionWithLinksForLimitedPartner(Transaction transaction,
                                                             String submissionUri,
                                                             Resource limitedPartnerResource,
                                                             String requestID) throws ServiceException {

        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnerResource));
        // Update the transaction
        transactionService.updateTransaction(transaction, requestID);

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

        limitedPartnerResource.setLinks(linksMap);
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);

        return limitedPartnerResource;
    }
}
