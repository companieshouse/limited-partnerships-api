package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@Service
public class GeneralPartnerService {

    private final GeneralPartnerRepository repository;
    private final GeneralPartnerMapper mapper;
    private final GeneralPartnerValidator generalPartnerValidator;
    private final TransactionService transactionService;

    public GeneralPartnerService(GeneralPartnerRepository repository,
                                 GeneralPartnerMapper mapper,
                                 GeneralPartnerValidator generalPartnerValidator,
                                 TransactionService transactionService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.generalPartnerValidator = generalPartnerValidator;
        this.transactionService = transactionService;
    }

    public String createGeneralPartner(Transaction transaction, GeneralPartnerDto generalPartnerDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        generalPartnerValidator.isValid(generalPartnerDto);

        GeneralPartnerDao dao = mapper.dtoToDao(generalPartnerDto);
        GeneralPartnerDao insertedSubmission = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String submissionUri = linkAndSaveDao(transaction, insertedSubmission.getId(), dao);
        updateTransactionWithGeneralPartnerTransactionResourceLinks(requestId, transaction, submissionUri);

        return insertedSubmission.getId();
    }

    private GeneralPartnerDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, GeneralPartnerDao dao) {
        dao.getData().setKind(FILING_KIND_GENERAL_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());

        GeneralPartnerDao insertedSubmission = repository.insert(dao);
        ApiLogger.infoContext(requestId, String.format("General Partner submission created with id: %s", insertedSubmission.getId()));
        return insertedSubmission;
    }

    private String linkAndSaveDao(Transaction transaction, String submissionId, GeneralPartnerDao dao) {
        var submissionUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), submissionId);
        dao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(dao);
        return submissionUri;
    }

    private void updateTransactionWithGeneralPartnerTransactionResourceLinks(
            String requestId, Transaction transaction, String submissionUri)  throws ServiceException {
        var generalPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);

        generalPartnerResource.setLinks(linksMap);
        generalPartnerResource.setKind(FILING_KIND_GENERAL_PARTNER);

        transaction.setResources(Collections.singletonMap(submissionUri, generalPartnerResource));

        transactionService.updateTransaction(transaction, requestId);
    }

    public void updateGeneralPartner(Transaction transaction, String generalPartnerId, GeneralPartnerDataDto generalPartnerDataDto, String requestId, String userId) throws ServiceException {
        var generalPartnerDaoBeforePatch = repository.findById(generalPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", generalPartnerId)));

        var generalPartnerDto = mapper.daoToDto(generalPartnerDaoBeforePatch);

        mapper.update(generalPartnerDataDto, generalPartnerDto.getData());

        var generalPartnerDaoAfterPatch = mapper.dtoToDao(generalPartnerDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(generalPartnerDaoBeforePatch, generalPartnerDaoAfterPatch);

        setAuditDetailsForUpdate(userId, generalPartnerDaoAfterPatch);

        ApiLogger.infoContext(requestId, String.format("General Partner updated with id: %s", generalPartnerId));

        repository.save(generalPartnerDaoAfterPatch);
    }

    private void copyMetaDataForUpdate(GeneralPartnerDao generalPartnerDaoBeforePatch,
                                       GeneralPartnerDao generalPartnerDaoAfterPatch) {
        generalPartnerDaoAfterPatch.setId(generalPartnerDaoBeforePatch.getId());
        generalPartnerDaoAfterPatch.setCreatedAt(generalPartnerDaoBeforePatch.getCreatedAt());
        generalPartnerDaoAfterPatch.setCreatedBy(generalPartnerDaoBeforePatch.getCreatedBy());
        generalPartnerDaoAfterPatch.setLinks(generalPartnerDaoBeforePatch.getLinks());
        generalPartnerDaoAfterPatch.setTransactionId(generalPartnerDaoBeforePatch.getTransactionId());
    }

    private void setAuditDetailsForUpdate(String userId, GeneralPartnerDao generalPartnerDaoAfterPatch) {
        generalPartnerDaoAfterPatch.setUpdatedAt(LocalDateTime.now());
        generalPartnerDaoAfterPatch.setUpdatedBy(userId);
    }
}

