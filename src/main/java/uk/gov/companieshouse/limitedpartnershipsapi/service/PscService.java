package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PscMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PscRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.Collections;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.*;

@Service
public class PscService {

    private final PscRepository repository;
    private final PscMapper mapper;
    private final TransactionService transactionService;

    public PscService(PscRepository repository,
                      PscMapper mapper,
                      TransactionService transactionService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionService = transactionService;
    }

    public String createPsc(Transaction transaction, PscDto pscDto, String requestId, String userId) throws ServiceException {
        PscDao dao = mapper.dtoToDao(pscDto);
        PscDao insertedSubmission = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String submissionUri = linkAndSaveDao(transaction, insertedSubmission.getId(), dao);

        String kind = requireNonNullElse(insertedSubmission.getData().getKind(), FILING_KIND_PSC);

        transactionService.updateTransactionWithLinksForResource(requestId, transaction, submissionUri, kind, null);

        return insertedSubmission.getId();
    }

    public void updatePsc(Transaction transaction, String pscId, PscDataDto pscChangesDataDto, String requestId, String userId) throws ResourceNotFoundException {
        var pscDaoBeforePatch = repository.findById(pscId).orElseThrow(() -> new ResourceNotFoundException(String.format("Person of significant control with id %s not found", pscId)));
        String kind = requireNonNullElse(pscDaoBeforePatch.getData().getKind(), FILING_KIND_PSC);
        checkPscIsLinkedToTransaction(transaction, pscId, kind);
        var pscDto = mapper.daoToDto(pscDaoBeforePatch);
        mapper.update(pscChangesDataDto, pscDto.getData());

        handleSecondNationalityOptionality(pscChangesDataDto, pscDto.getData());
        var pscDaoAfterPatch = mapper.dtoToDao(pscDto);
        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForPatch(pscDaoBeforePatch, pscDaoAfterPatch, userId);

        ApiLogger.infoContext(requestId, String.format("Person of significant control updated with id: %s", pscId));

        repository.save(pscDaoAfterPatch);
    }

    private PscDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, PscDao dao) {
        if (dao.getData().getKind() == null) {
            dao.getData().setKind(FILING_KIND_PSC);
        }

        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedBy(userId);
        dao.setUpdatedBy(userId);
        dao.setTransactionId(transaction.getId());

        PscDao insertedSubmission = repository.insert(dao);
        ApiLogger.infoContext(requestId, String.format("Person of significant control submission created with id: %s", insertedSubmission.getId()));
        return insertedSubmission;
    }

    private String linkAndSaveDao(Transaction transaction, String submissionId, PscDao dao) {
        var submissionUri = String.format(URL_GET_PSC, transaction.getId(), submissionId);
        dao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(dao);
        return submissionUri;
    }

    private void checkPscIsLinkedToTransaction(Transaction transaction, String pscId, String kind) throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        var pscUri = String.format(URL_GET_PSC, transactionId, pscId);

        if (!transactionService.isTransactionLinkedToResource(transaction, pscUri, kind)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches person of significant control id: %s", transactionId, pscId));
        }
    }

    private void handleSecondNationalityOptionality(PscDataDto pscChangesDataDto,
                                                    PscDataDto pscDataDto) {
        // The first 'not null' check here ensures that second nationality isn't wiped if, for example, only address data is being updated
        if (pscChangesDataDto.getNationality1() != null && pscChangesDataDto.getNationality2() == null) {
            pscDataDto.setNationality2(null);
        }
    }

    private void copyMetaDataForPatch(PscDao pscDaoBeforePatch,
                                      PscDao pscDaoAfterPatch,
                                      String userId) {
        pscDaoAfterPatch.setId(pscDaoBeforePatch.getId());
        pscDaoAfterPatch.setCreatedAt(pscDaoBeforePatch.getCreatedAt());
        pscDaoAfterPatch.setCreatedBy(pscDaoBeforePatch.getCreatedBy());
        pscDaoAfterPatch.setLinks(pscDaoBeforePatch.getLinks());
        pscDaoAfterPatch.setTransactionId(pscDaoBeforePatch.getTransactionId());
        pscDaoAfterPatch.setUpdatedBy(userId);
    }
}
