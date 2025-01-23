package uk.gov.companieshouse.limitedpartnershipsapi.service;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;


@Service
public class LimitedPartnershipIncorporationService {

    private final LimitedPartnershipIncorporationRepository repository;
    private final TransactionService transactionService;

    public LimitedPartnershipIncorporationService(
            LimitedPartnershipIncorporationRepository repository,
            TransactionService transactionService) {
        this.repository = repository;
        this.transactionService = transactionService;
    }

    public String createIncorporation(Transaction transaction, String requestId, String userId)
            throws ServiceException {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.getData().setKind(FILING_KIND_REGISTRATION);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);

        LimitedPartnershipIncorporationDao insertedIncorporation = repository.insert(dao);

        String incorporationUri = getSubmissionUri(transaction.getId(), insertedIncorporation.getId());
        updateIncorporationTypeWithSelfLink(dao, incorporationUri);

        updateTransactionWithIncorporationResource(transaction,
                incorporationUri, requestId);

        return insertedIncorporation.getId();
    }

    private void updateTransactionWithIncorporationResource(Transaction transaction, String incorporationUri, String loggingContext)
            throws ServiceException {
        var incorporationTransactionResource = createIncorporationTransactionResource(incorporationUri);

        // TODO set filing_mode on transaction (requires update to SDK)
        transaction.setResources(Collections.singletonMap(incorporationUri, incorporationTransactionResource));
        transactionService.updateTransaction(transaction, loggingContext);
    }

    private Resource createIncorporationTransactionResource(String incorporationUri) {
        var incorporationResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", incorporationUri);

        incorporationResource.setLinks(linksMap);
        incorporationResource.setKind(FILING_KIND_REGISTRATION);

        return incorporationResource;
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
