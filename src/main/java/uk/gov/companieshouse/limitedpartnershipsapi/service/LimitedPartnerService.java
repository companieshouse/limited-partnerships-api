package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
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

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId)
            throws ServiceException {
        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnerDao insertedLimitedPartner = repository.insert(dao);

        transaction.setFilingMode(FILING_KIND_LIMITED_PARTNER);

        String limitedpartnerUri = getSubmissionUri(transaction.getId(), insertedLimitedPartner.getId());
        updateLimitedPartnerTypeWithSelfLink(dao, limitedpartnerUri);

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

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_LIMITED_PARTNER, transactionId, submissionId);
    }
}
