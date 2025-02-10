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
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
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

    private final LimitedPartnerMapper mapper;

    private final TransactionUtils transactionUtils;

    public LimitedPartnerService(
            LimitedPartnerRepository repository,
            LimitedPartnerMapper mapper,
            TransactionUtils transactionUtils) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
    }

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId) throws ServiceException {

        if (hasExistingLimitedPartnerSubmission(transaction)) {
            throw new ServiceException(String.format(
                    "The transaction with id %s already has a Limited Partnership submission associated with it", transaction.getId()));
        }

        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnerDao insertedLimitedPartner = repository.insert(dao);
        LimitedPartnerDao insertedSubmission = repository.insert(dao);

        ApiLogger.infoContext(requestId, String.format("Limited Partnership submission created with id: %s", insertedSubmission.getId()));

        transaction.setFilingMode(FILING_KIND_LIMITED_PARTNER);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedSubmission.getId());
        var limitedPartnerResource = createLimitedPartnerTransactionResource(submissionUri);

        String limitedpartnerUri = getSubmissionUri(transaction.getId(), insertedLimitedPartner.getId());
        updateLimitedPartnerTypeWithSelfLink(dao, limitedpartnerUri);
        updateTransactionWithLinksAndPartnerName(transaction, requestId, submissionUri, limitedPartnerResource, requestId);

        return insertedLimitedPartner.getId();
    }

    public LimitedPartnerDto getLimitedPartner(Transaction transaction, String submissionId) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), submissionId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches submission id: %s", transaction.getId(), submissionId));
        }

        var submission = repository.findById(submissionId);
        LimitedPartnerDao submissionDao = submission.orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", submissionId)));
        return mapper.daoToDto(submissionDao);
    }

    public LimitedPartnerDto getLimitedPartner(Transaction transaction) throws ServiceException {
        if (!transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a limited partnership resource", transaction.getId()));
        }

        var submissions = repository.findByTransactionId(transaction.getId());

        if (submissions.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No limited partnership found for transaction id %s", transaction.getId()));
        } else if (submissions.size() > 1) {
            throw new ServiceException(String.format("More than one limited partnership found for transaction id %s", transaction.getId()));
        }

        LimitedPartnerDao submissionDao = submissions.getFirst();

        return mapper.daoToDto(submissionDao);
    }

    private void updateLimitedPartnerTypeWithSelfLink(LimitedPartnerDao limitedPartnerDao,
                                                      String submissionUri) {
        limitedPartnerDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(limitedPartnerDao);
    }

    private void updateTransactionWithLinksAndPartnerName(Transaction transaction,
                                                          String requestId,
                                                          String submissionUri,
                                                          Resource limitedPartnerResource,
                                                          String loggingContext) {
        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnerResource));
        ApiLogger.infoContext(requestId, String.format("Updating transaction with submissionUri:: %s", loggingContext));

    }

    private Resource createLimitedPartnerTransactionResource(String submissionUri) {
        var limitedPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);

        limitedPartnerResource.setLinks(linksMap);
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);

        return limitedPartnerResource;
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId);
    }

    private boolean hasExistingLimitedPartnerSubmission(Transaction transaction) {
        if (transaction.getResources() != null) {
            return transaction.getResources().entrySet().stream().anyMatch(
                    resourceEntry -> FILING_KIND_LIMITED_PARTNER.equals(resourceEntry.getValue().getKind()));
        }
        return false;
    }
}
