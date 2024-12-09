package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.DataType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SUBMISSION_URI_PATTERN;

@Service
public class LimitedPartnershipService {

    private final LimitedPartnershipMapper mapper;
    private final LimitedPartnershipSubmissionsRepository repository;
    private final TransactionService transactionService;

    @Autowired
    public LimitedPartnershipService(LimitedPartnershipMapper mapper,
                                     LimitedPartnershipSubmissionsRepository repository,
                                     TransactionService transactionService) {
        this.mapper = mapper;
        this.repository = repository;
        this.transactionService = transactionService;
    }

    public String createLimitedPartnership(Transaction transaction,
                                           LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
                                           String requestId,
                                           String userId) throws ServiceException {
        ApiLogger.debug("Called createLimitedPartnership(...)");

        if (hasExistingLimitedPartnershipSubmission(transaction)) {
            throw new ServiceException(String.format(
                    "The transaction with id %s already has a Limited Partnership submission associated with it", transaction.getId()));
        }

        LimitedPartnershipSubmissionDao dao = mapper.dtoToDao(limitedPartnershipSubmissionDto);
        dao.setCreatedAt(LocalDateTime.now());
        dao.setUserId(userId);

        LimitedPartnershipSubmissionDao insertedSubmission = repository.insert(dao);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedSubmission.getId());
        updateLimitedPartnershipSubmissionWithSelfLink(insertedSubmission, submissionUri); 

        // Create the Resource to be added to the Transaction (includes various links to the resource)
        var limitedPartnershipResource = createLimitedPartnershipTransactionResource(submissionUri);

        // Update company name set on the transaction and add a link to the newly created Limited Partnership
        // submission (aka resource) to the transaction
        updateTransactionWithLinksAndPartnershipName(transaction, limitedPartnershipSubmissionDto,
                submissionUri, limitedPartnershipResource, requestId);

        ApiLogger.infoContext(requestId, String.format("Limited Partnership submission created with id: %s", insertedSubmission.getId()));

        return insertedSubmission.getId();
    }

    public void updateLimitedPartnership(String submissionId,
                                         DataType type,
                                         Map<String, Object> data) throws ServiceException {

        var limitedPartnershipSubmissionDao = repository.findById(submissionId);

        if (limitedPartnershipSubmissionDao.isEmpty()) {
            throw new ServiceException(String.format(
                    "Submission with id %s not found", submissionId));
        }

        if (type == DataType.EMAIL) {
            DataDao dataDao = limitedPartnershipSubmissionDao.get().getData();

            String email = (String) data.get("email");
            dataDao.setEmail(email);

            repository.save(limitedPartnershipSubmissionDao.get());
        }
    }

    private Resource createLimitedPartnershipTransactionResource(String submissionUri) {
        var limitedPartnershipResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);

        // TODO Add 'validation status' and 'cost' links here later

        limitedPartnershipResource.setLinks(linksMap);
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        return limitedPartnershipResource;
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(SUBMISSION_URI_PATTERN, transactionId, submissionId);
    }

    private void updateTransactionWithLinksAndPartnershipName(Transaction transaction,
                                                              LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
                                                              String submissionUri,
                                                              Resource limitedPartnershipResource,
                                                              String loggingContext) throws ServiceException {
        transaction.setCompanyName(limitedPartnershipSubmissionDto.getData().getPartnershipName());
        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnershipResource));

        transactionService.updateTransaction(transaction, loggingContext);
    }

    private boolean hasExistingLimitedPartnershipSubmission(Transaction transaction) {
        if (transaction.getResources() != null) {
            return transaction.getResources().entrySet().stream().anyMatch(
                    resourceEntry -> FILING_KIND_LIMITED_PARTNERSHIP.equals(resourceEntry.getValue().getKind()));
        }
        return false;
    }

    private void updateLimitedPartnershipSubmissionWithSelfLink(LimitedPartnershipSubmissionDao submission,
                                                              String submissionUri) {
        submission.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(submission);
    }
}
