package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipPatchMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_RESUME_REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.VALIDATION_STATUS_URI_SUFFIX;

@Service
public class LimitedPartnershipService {

    private final LimitedPartnershipMapper mapper;
    private final LimitedPartnershipPatchMapper patchMapper;
    private final LimitedPartnershipSubmissionsRepository repository;
    private final TransactionService transactionService;
    private final TransactionUtils transactionUtils;

    @Autowired
    public LimitedPartnershipService(LimitedPartnershipMapper mapper,
                                     LimitedPartnershipPatchMapper patchMapper,
                                     LimitedPartnershipSubmissionsRepository repository,
                                     TransactionService transactionService,
                                     TransactionUtils transactionUtils) {
        this.mapper = mapper;
        this.patchMapper = patchMapper;
        this.repository = repository;
        this.transactionService = transactionService;
        this.transactionUtils = transactionUtils;
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
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());

        LimitedPartnershipSubmissionDao insertedSubmission = repository.insert(dao);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedSubmission.getId());
        updateLimitedPartnershipSubmissionWithSelfLink(insertedSubmission, submissionUri);

        // Create the Resource to be added to the Transaction (includes various links to the resource)
        var limitedPartnershipResource = createLimitedPartnershipTransactionResource(submissionUri);

        updateTransactionWithLinksAndPartnershipName(transaction, limitedPartnershipSubmissionDto,
                submissionUri, limitedPartnershipResource, requestId, insertedSubmission.getId());

        ApiLogger.infoContext(requestId, String.format("Limited Partnership submission created with id: %s", insertedSubmission.getId()));

        return insertedSubmission.getId();
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

    private void copyMetaDataForUpdate(LimitedPartnershipSubmissionDao lpSubmissionDaoBeforePatch,
                                       LimitedPartnershipSubmissionDao lpSubmissionDaoAfterPatch) {
        lpSubmissionDaoAfterPatch.setId(lpSubmissionDaoBeforePatch.getId());
        lpSubmissionDaoAfterPatch.setCreatedAt(lpSubmissionDaoBeforePatch.getCreatedAt());
        lpSubmissionDaoAfterPatch.setCreatedBy(lpSubmissionDaoBeforePatch.getCreatedBy());
        lpSubmissionDaoAfterPatch.setLinks(lpSubmissionDaoBeforePatch.getLinks());
        lpSubmissionDaoAfterPatch.setTransactionId(lpSubmissionDaoBeforePatch.getTransactionId());
    }

    private void setAuditDetailsForUpdate(String userId, LimitedPartnershipSubmissionDao lpSubmissionDaoAfterPatch) {
        lpSubmissionDaoAfterPatch.setUpdatedAt(LocalDateTime.now());
        lpSubmissionDaoAfterPatch.setUpdatedBy(userId);
    }

    private Resource createLimitedPartnershipTransactionResource(String submissionUri) {
        var limitedPartnershipResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        linksMap.put("validation_status", submissionUri + VALIDATION_STATUS_URI_SUFFIX);

        // TODO Add 'cost' link here later

        limitedPartnershipResource.setLinks(linksMap);
        limitedPartnershipResource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        return limitedPartnershipResource;
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_PARTNERSHIP, transactionId, submissionId);
    }

    /**
     * Update company name set on the transaction and add a link to the newly created Limited Partnership
     * submission (aka resource) to the transaction. A resume link (URL) is also created and added, which
     * is handled by the web client.
     */
    private void updateTransactionWithLinksAndPartnershipName(Transaction transaction,
                                                              LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto,
                                                              String submissionUri,
                                                              Resource limitedPartnershipResource,
                                                              String loggingContext,
                                                              String submissionId) throws ServiceException {
        transaction.setCompanyName(limitedPartnershipSubmissionDto.getData().getPartnershipName());
        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnershipResource));

        final var resumeJourneyUri = String.format(URL_RESUME_REGISTRATION, transaction.getId(), submissionId);
        transaction.setResumeJourneyUri(resumeJourneyUri);

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

    public LimitedPartnershipSubmissionDto getLimitedPartnership(Transaction transaction, String submissionId) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), submissionId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches submission id: %s", transaction.getId(), submissionId));
        }

        var submission = repository.findById(submissionId);
        LimitedPartnershipSubmissionDao submissionDao = submission.orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", submissionId)));
        return mapper.daoToDto(submissionDao);
    }

    public LimitedPartnershipSubmissionDto getLimitedPartnership(Transaction transaction) throws ServiceException {
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

        LimitedPartnershipSubmissionDao submissionDao = submissions.getFirst();

        return mapper.daoToDto(submissionDao);
    }

    public List<String> validateLimitedPartnership(Transaction transaction, String submissionId)
            throws ResourceNotFoundException {
        LimitedPartnershipSubmissionDto dto = getLimitedPartnership(transaction, submissionId);

        // TODO See if it's possible to auto-wire in a validator from Spring Boot
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<LimitedPartnershipSubmissionDto>> violations = validator.validate(dto);

        List violationsList = new ArrayList<>();
        violations.stream().forEach(v -> violationsList.add(v.getMessage()));

        // TODO Create a new validator class (or just method in here?) that checks the mandatory fields. For now, just
        //      simulate a missing email address
        violationsList.add("Email is required");

        return violationsList;
    }
}
