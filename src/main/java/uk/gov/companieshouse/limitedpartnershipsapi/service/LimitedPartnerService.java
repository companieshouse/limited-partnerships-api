package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

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

    private final TransactionUtils transactionUtils;

    public LimitedPartnerService(
            LimitedPartnerRepository repository,
            LimitedPartnerMapper mapper,
            TransactionUtils transactionUtils,
            TransactionService transactionService) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
        this.transactionService = transactionService;
    }

    public String createLimitedPartner(Transaction transaction, String requestId, String userId)
            throws ServiceException {
        var dao = new LimitedPartnerDao();
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnerDao insertedLimitedPartner = repository.insert(dao);

        transaction.setFilingMode(FILING_KIND_LIMITED_PARTNER);

        String limitedpartnerUri = getSubmissionUri(transaction.getId(), insertedLimitedPartner.getId());
        updateLimitedPartnerTypeWithSelfLink(dao, limitedpartnerUri);

        updateTransactionWithLimitedPartnerResource(transaction,
                limitedpartnerUri, requestId);

        return insertedLimitedPartner.getId();
    }

    private void updateTransactionWithLimitedPartnerResource(Transaction transaction, String limitedpartnerUri, String loggingContext)
            throws ServiceException {
        var limitedPartnerTransactionResource = createLimitedPartnerTransactionResource(limitedpartnerUri);

        // TODO set filing_mode on transaction (requires update to SDK)
        transaction.setResources(Collections.singletonMap(limitedpartnerUri, limitedPartnerTransactionResource));
        transactionService.updateTransaction(transaction, loggingContext);
    }

    private Resource createLimitedPartnerTransactionResource(String limitedPartnerUri) {
        var limitedPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", limitedPartnerUri);

        limitedPartnerResource.setLinks(linksMap);
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);

        return limitedPartnerResource;
    }

    public LimitedPartnerDto getLimitedPartner(Transaction transaction,
                                               String filingResourceId,
                                               boolean includeSubResources) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), filingResourceId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnerSubmission(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches limited partner id: %s", transaction.getId(), filingResourceId));
        }

        var submission = repository.findById(filingResourceId);
        LimitedPartnerDao limitedPartnerDao = submission.orElseThrow(() -> new ResourceNotFoundException(String.format("Limited Partner with id %s not found", filingResourceId)));

        // TODO Use value of 'includeSubResources' to retrieve further LP data, if set to 'true'

        return mapper.daoToDto(limitedPartnerDao);
    }

    private void updateLimitedPartnerTypeWithSelfLink(LimitedPartnerDao limitedPartnerDao,
                                                      String submissionUri) {
        limitedPartnerDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(limitedPartnerDao);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId);
    }
}
