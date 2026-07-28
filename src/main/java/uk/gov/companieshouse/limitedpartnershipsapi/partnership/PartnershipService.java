package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.PostTransitionStrategyHandler;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.COSTS_URI_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_COSTS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.Operation.INSERTION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.Operation.UPDATE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.executeWithTransactionalRollback;

@Service
public class PartnershipService {

    private final PartnershipMapper mapper;
    private final PartnershipPatchMapper patchMapper;
    private final PartnershipRepository repository;
    private final TransactionService transactionService;
    private final LimitedPartnershipValidator limitedPartnershipValidator;
    private final PostTransitionStrategyHandler postTransitionStrategyHandler;

    @Autowired
    public PartnershipService(PartnershipMapper mapper,
                              PartnershipPatchMapper patchMapper,
                              PartnershipRepository repository,
                              TransactionService transactionService,
                              LimitedPartnershipValidator limitedPartnershipValidator,
                              PostTransitionStrategyHandler postTransitionStrategyHandler) {
        this.mapper = mapper;
        this.patchMapper = patchMapper;
        this.repository = repository;
        this.transactionService = transactionService;
        this.limitedPartnershipValidator = limitedPartnershipValidator;
        this.postTransitionStrategyHandler = postTransitionStrategyHandler;
    }

    public String createLimitedPartnership(Transaction transaction,
                                           PartnershipDto partnershipDto,
                                           String requestId,
                                           String userId)
            throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        ApiLogger.debug("Called createLimitedPartnership(...)");

        limitedPartnershipValidator.validatePartial(partnershipDto, FilingMode.fromDescription(transaction.getFilingMode()));

        if (transactionService.hasExistingLimitedPartnership(transaction)) {
            throw new ServiceException(String.format(
                    "The transaction with id %s already has a Limited Partnership associated with it", transaction.getId()));
        }

        PartnershipDao dao = mapper.dtoToDao(partnershipDto);
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());

        PartnershipDao insertedLimitedPartnership = repository.insert(dao);

        final String submissionUri = getSubmissionUri(transaction.getId(), insertedLimitedPartnership.getId());
        updateLimitedPartnershipWithSelfLink(insertedLimitedPartnership, submissionUri);

        String kind = requireNonNullElse(insertedLimitedPartnership.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);

        // Create the Resource to be added to the Transaction (includes various links to the resource)
        var limitedPartnershipResource = transactionService.createLimitedPartnershipTransactionResource(transaction, submissionUri, kind);

        if (FilingMode.DEFAULT.getDescription().equals(transaction.getFilingMode())) {
            addCostLink(partnershipDto, limitedPartnershipResource, submissionUri);
        }

        executeWithTransactionalRollback(
            requestId,
            insertedLimitedPartnership.getId(),
            () -> transactionService.updateTransactionWithLinksAndPartnershipName(transaction, partnershipDto,
                submissionUri, limitedPartnershipResource, requestId, insertedLimitedPartnership.getId()),
            INSERTION,
            () -> repository.deleteById(insertedLimitedPartnership.getId()));

        ApiLogger.infoContext(requestId, String.format("Limited Partnership created with id: %s", insertedLimitedPartnership.getId()));

        return insertedLimitedPartnership.getId();
    }

    private void addCostLink(PartnershipDto partnershipDto, Resource limitedPartnershipResource, String submissionUri) throws ServiceException {
        Cost cost = postTransitionStrategyHandler.getCost(partnershipDto);
        if (cost != null) {
            limitedPartnershipResource.getLinks().put(LINK_COSTS, submissionUri + COSTS_URI_SUFFIX);
        }
    }

    public void updateLimitedPartnership(Transaction transaction,
                                         String submissionId,
                                         PartnershipPatchDto partnershipPatchDto,
                                         String requestId,
                                         String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var lpSubmissionDaoBeforePatch = repository.findById(submissionId).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", submissionId)));

        checkIfPartnershipIsLinkedToTransaction(transaction, submissionId, lpSubmissionDaoBeforePatch);

        var lpSubmissionDto = mapper.daoToDto(lpSubmissionDaoBeforePatch);

        patchMapper.update(partnershipPatchDto, lpSubmissionDto.getData());

        limitedPartnershipValidator.validateUpdate(lpSubmissionDto, transaction);

        var lpSubmissionDaoAfterPatch = mapper.dtoToDao(lpSubmissionDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(lpSubmissionDaoBeforePatch, lpSubmissionDaoAfterPatch);

        setAuditDetailsForUpdate(userId, lpSubmissionDaoAfterPatch);

        repository.save(lpSubmissionDaoAfterPatch);

        executeWithTransactionalRollback(
            requestId,
            submissionId,
            () -> transactionService.updateTransactionWithPartnershipName(transaction, requestId, lpSubmissionDaoAfterPatch.getData().getPartnershipName()),
            UPDATE,
            () -> repository.save(lpSubmissionDaoBeforePatch));

        ApiLogger.infoContext(requestId, String.format("Limited Partnership submission updated with id: %s", submissionId));
    }

    private void copyMetaDataForUpdate(PartnershipDao lpSubmissionDaoBeforePatch,
                                       PartnershipDao lpSubmissionDaoAfterPatch) {
        lpSubmissionDaoAfterPatch.setId(lpSubmissionDaoBeforePatch.getId());
        lpSubmissionDaoAfterPatch.setCreatedAt(lpSubmissionDaoBeforePatch.getCreatedAt());
        lpSubmissionDaoAfterPatch.setCreatedBy(lpSubmissionDaoBeforePatch.getCreatedBy());
        lpSubmissionDaoAfterPatch.setLinks(lpSubmissionDaoBeforePatch.getLinks());
        lpSubmissionDaoAfterPatch.setTransactionId(lpSubmissionDaoBeforePatch.getTransactionId());
    }

    private void setAuditDetailsForUpdate(String userId, PartnershipDao lpSubmissionDaoAfterPatch) {
        lpSubmissionDaoAfterPatch.setUpdatedBy(userId);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_PARTNERSHIP, transactionId, submissionId);
    }

    private void updateLimitedPartnershipWithSelfLink(PartnershipDao partnershipDao,
                                                      String submissionUri) {
        partnershipDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(partnershipDao);
    }

    public PartnershipDto getLimitedPartnership(Transaction transaction, String submissionId) throws ResourceNotFoundException {
        var limitedPartnershipDao = repository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Limited Partnership with id %s not found", submissionId)));

        checkIfPartnershipIsLinkedToTransaction(transaction, submissionId, limitedPartnershipDao);

        return mapper.daoToDto(limitedPartnershipDao);
    }

    public PartnershipDto getLimitedPartnership(Transaction transaction) throws ServiceException {
        var limitedPartnerships = repository.findByTransactionId(transaction.getId());

        if (limitedPartnerships.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No limited partnership found for transaction id %s", transaction.getId()));
        } else if (limitedPartnerships.size() > 1) {
            throw new ServiceException(String.format("More than one limited partnership found for transaction id %s", transaction.getId()));
        }

        var limitedPartnershipDao = limitedPartnerships.getFirst();

        String kind = requireNonNullElse(limitedPartnershipDao.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);

        if (!transactionService.doesTransactionHaveALimitedPartnership(transaction, kind)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a limited partnership resource", transaction.getId()));
        }

        return mapper.daoToDto(limitedPartnershipDao);
    }

    public List<ValidationStatusError> validateLimitedPartnership(Transaction transaction)
            throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PartnershipDto partnershipDto = getLimitedPartnership(transaction);

        if (FilingMode.DEFAULT.getDescription().equals(transaction.getFilingMode())) {
            return postTransitionStrategyHandler.validateLimitedPartnership(partnershipDto, transaction);
        }

        return limitedPartnershipValidator.validateFull(partnershipDto, FilingMode.fromDescription(transaction.getFilingMode()));
    }

    private void checkIfPartnershipIsLinkedToTransaction(Transaction transaction, String submissionId, PartnershipDao partnershipDao) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), submissionId);
        String kind = requireNonNullElse(partnershipDao.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);

        if (!transactionService.isTransactionLinkedToResource(transaction, submissionUri, kind)) {
            throw new ResourceNotFoundException(String.format(
                "Transaction id: %s does not have a resource that matches submission id: %s", transaction.getId(), submissionId));
        }
    }
}
