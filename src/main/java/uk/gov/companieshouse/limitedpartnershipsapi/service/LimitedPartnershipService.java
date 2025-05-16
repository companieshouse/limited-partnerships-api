package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipPatchMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_RESUME_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.VALIDATION_STATUS_URI_SUFFIX;

@Service
public class LimitedPartnershipService {

    private final LimitedPartnershipMapper mapper;
    private final LimitedPartnershipPatchMapper patchMapper;
    private final LimitedPartnershipRepository repository;
    private final TransactionService transactionService;
    private final TransactionUtils transactionUtils;
    private final LimitedPartnershipValidator limitedPartnershipValidator;

    @Autowired
    public LimitedPartnershipService(LimitedPartnershipMapper mapper,
                                     LimitedPartnershipPatchMapper patchMapper,
                                     LimitedPartnershipRepository repository,
                                     TransactionService transactionService,
                                     TransactionUtils transactionUtils,
                                     LimitedPartnershipValidator limitedPartnershipValidator) {
        this.mapper = mapper;
        this.patchMapper = patchMapper;
        this.repository = repository;
        this.transactionService = transactionService;
        this.transactionUtils = transactionUtils;
        this.limitedPartnershipValidator = limitedPartnershipValidator;
    }

    public String createLimitedPartnership(Transaction transaction,
                                           LimitedPartnershipDto limitedPartnershipDto,
                                           String requestId,
                                           String userId) throws ServiceException {
        ApiLogger.debug("Called createLimitedPartnership(...)");

        if (hasExistingLimitedPartnership(transaction)) {
            throw new ServiceException(String.format(
                    "The transaction with id %s already has a Limited Partnership associated with it", transaction.getId()));
        }

        LimitedPartnershipDao dao = mapper.dtoToDao(limitedPartnershipDto);
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());

        LimitedPartnershipDao insertedLimitedPartnership = repository.insert(dao);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedLimitedPartnership.getId());
        updateLimitedPartnershipWithSelfLink(insertedLimitedPartnership, submissionUri);

        // Create the Resource to be added to the Transaction (includes various links to the resource)
        var limitedPartnershipResource = createLimitedPartnershipTransactionResource(submissionUri);

        updateTransactionWithLinksAndPartnershipName(transaction, limitedPartnershipDto,
                submissionUri, limitedPartnershipResource, requestId, insertedLimitedPartnership.getId());

        ApiLogger.infoContext(requestId, String.format("Limited Partnership created with id: %s", insertedLimitedPartnership.getId()));

        return insertedLimitedPartnership.getId();
    }

    public void updateLimitedPartnership(Transaction transaction,
                                         String submissionId,
                                         LimitedPartnershipPatchDto limitedPartnershipPatchDto,
                                         String requestId,
                                         String userId) throws ServiceException {
        var optionalLpSubmissionDaoBeforePatch = repository.findById(submissionId);

        if (optionalLpSubmissionDaoBeforePatch.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Submission with id %s not found", submissionId));
        }

        var lpSubmissionDaoBeforePatch = optionalLpSubmissionDaoBeforePatch.get();
        var lpSubmissionDto = mapper.daoToDto(lpSubmissionDaoBeforePatch);

        patchMapper.update(limitedPartnershipPatchDto, lpSubmissionDto.getData());

        var lpSubmissionDaoAfterPatch = mapper.dtoToDao(lpSubmissionDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(lpSubmissionDaoBeforePatch, lpSubmissionDaoAfterPatch);

        setAuditDetailsForUpdate(userId, lpSubmissionDaoAfterPatch);

        // Finally, update the transaction in case the partnership name has changed as a result of this patch request
        transactionService.updateTransactionWithPartnershipName(transaction, requestId, lpSubmissionDaoAfterPatch.getData().getPartnershipName());

        ApiLogger.infoContext(requestId, String.format("Limited Partnership submission updated with id: %s", submissionId));

        repository.save(lpSubmissionDaoAfterPatch);
    }

    private void copyMetaDataForUpdate(LimitedPartnershipDao lpSubmissionDaoBeforePatch,
                                       LimitedPartnershipDao lpSubmissionDaoAfterPatch) {
        lpSubmissionDaoAfterPatch.setId(lpSubmissionDaoBeforePatch.getId());
        lpSubmissionDaoAfterPatch.setCreatedAt(lpSubmissionDaoBeforePatch.getCreatedAt());
        lpSubmissionDaoAfterPatch.setCreatedBy(lpSubmissionDaoBeforePatch.getCreatedBy());
        lpSubmissionDaoAfterPatch.setLinks(lpSubmissionDaoBeforePatch.getLinks());
        lpSubmissionDaoAfterPatch.setTransactionId(lpSubmissionDaoBeforePatch.getTransactionId());
    }

    private void setAuditDetailsForUpdate(String userId, LimitedPartnershipDao lpSubmissionDaoAfterPatch) {
        lpSubmissionDaoAfterPatch.setUpdatedAt(LocalDateTime.now());
        lpSubmissionDaoAfterPatch.setUpdatedBy(userId);
    }

    private Resource createLimitedPartnershipTransactionResource(String submissionUri) {
        var limitedPartnershipResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        linksMap.put("validation_status", submissionUri + VALIDATION_STATUS_URI_SUFFIX);
        linksMap.put("costs", submissionUri + "/costs");

        limitedPartnershipResource.setLinks(linksMap);
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        return limitedPartnershipResource;
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_PARTNERSHIP, transactionId, submissionId);
    }

    /**
     * Update company name set on the transaction and add a link to the newly created Limited Partnership
     * resource to the transaction. A resume link (URL) is also created and added, which
     * is handled by the web client.
     */
    private void updateTransactionWithLinksAndPartnershipName(Transaction transaction,
                                                              LimitedPartnershipDto limitedPartnershipDto,
                                                              String submissionUri,
                                                              Resource limitedPartnershipResource,
                                                              String loggingContext,
                                                              String submissionId) throws ServiceException {
        transaction.setCompanyName(limitedPartnershipDto.getData().getPartnershipName());
        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnershipResource));

        final var resumeJourneyUri = String.format(URL_RESUME_PARTNERSHIP, transaction.getId(), submissionId);
        transaction.setResumeJourneyUri(resumeJourneyUri);

        transactionService.updateTransaction(transaction, loggingContext);
    }

    private boolean hasExistingLimitedPartnership(Transaction transaction) {
        if (transaction.getResources() != null) {
            return transaction.getResources().entrySet().stream().anyMatch(
                    resourceEntry -> FILING_KIND_LIMITED_PARTNERSHIP.equals(resourceEntry.getValue().getKind()));
        }
        return false;
    }

    private void updateLimitedPartnershipWithSelfLink(LimitedPartnershipDao limitedPartnershipDao,
                                                      String submissionUri) {
        limitedPartnershipDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(limitedPartnershipDao);
    }

    public LimitedPartnershipDto getLimitedPartnership(Transaction transaction, String submissionId) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), submissionId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnership(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches submission id: %s", transaction.getId(), submissionId));
        }

        var submission = repository.findById(submissionId);
        var limitedPartnershipDao = submission.orElseThrow(() -> new ResourceNotFoundException(String.format("Limited Partnership with id %s not found", submissionId)));
        return mapper.daoToDto(limitedPartnershipDao);
    }

    public LimitedPartnershipDto getLimitedPartnership(Transaction transaction) throws ServiceException {
        if (!transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a limited partnership resource", transaction.getId()));
        }

        var limitedPartnerships = repository.findByTransactionId(transaction.getId());

        if (limitedPartnerships.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No limited partnership found for transaction id %s", transaction.getId()));
        } else if (limitedPartnerships.size() > 1) {
            throw new ServiceException(String.format("More than one limited partnership found for transaction id %s", transaction.getId()));
        }

        var limitedPartnershipDao = limitedPartnerships.getFirst();

        return mapper.daoToDto(limitedPartnershipDao);
    }

    public List<ValidationStatusError> validateLimitedPartnership(Transaction transaction, String submissionId)
            throws ResourceNotFoundException {
        LimitedPartnershipDto dto = getLimitedPartnership(transaction, submissionId);

        return limitedPartnershipValidator.validate(dto);
    }
}
