package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

@Service
public class LimitedPartnershipIncorporationService {

    public static final String LIMITED_PARTNERSHIP_REGISTRATION_KIND = "limited-partnership-registration";

    private final LimitedPartnershipIncorporationRepository repository;

    private final LimitedPartnershipIncorporationMapper mapper;

    private final TransactionUtils transactionUtils;

    public LimitedPartnershipIncorporationService(
            LimitedPartnershipIncorporationRepository repository,
            LimitedPartnershipIncorporationMapper mapper,
            TransactionUtils transactionUtils) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionUtils = transactionUtils;
    }

    public String createIncorporation(String userId, String transaction) {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.getData().setKind(LIMITED_PARTNERSHIP_REGISTRATION_KIND);
        // TODO set etag
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        LimitedPartnershipIncorporationDao insertedIncorporation = repository.insert(dao);

        String incorporationUri = getSubmissionUri(transaction, insertedIncorporation.getId());
        updateIncorporationTypeWithSelfLink(dao, incorporationUri);

        // TODO Update transaction with master_resource
        return insertedIncorporation.getId();
    }


    public LimitedPartnershipIncorporationDto getIncorporation(Transaction transaction,
                                                               String filingResourceId,
                                                               boolean includeSubResources) throws ResourceNotFoundException {
        String submissionUri = getSubmissionUri(transaction.getId(), filingResourceId);
        if (!transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(transaction, submissionUri)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches incorporation id: %s", transaction.getId(), filingResourceId));
        }

        var submission = repository.findById(filingResourceId);
        LimitedPartnershipIncorporationDao incorporationDao = submission.orElseThrow(() -> new ResourceNotFoundException(String.format("Incorporation with id %s not found", filingResourceId)));

        // TODO Use value of 'includeSubResources' to retrieve further LP data, if set to 'true'

        return mapper.daoToDto(incorporationDao);
    }


    private void updateIncorporationTypeWithSelfLink(LimitedPartnershipIncorporationDao incorporationDao,
                                                     String submissionUri) {
        incorporationDao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(incorporationDao);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(URL_GET_INCORPORATION, transactionId, submissionId);
    }
}
