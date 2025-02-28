package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
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
    private final TransactionService transactionService;
    private final GeneralPartnerValidator generalPartnerValidator;

    public GeneralPartnerService(GeneralPartnerRepository repository,
                                 GeneralPartnerMapper mapper,
                                 TransactionService transactionService,
                                 GeneralPartnerValidator generalPartnerValidator) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionService = transactionService;
        this.generalPartnerValidator = generalPartnerValidator;
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
}
