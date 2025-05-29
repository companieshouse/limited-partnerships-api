package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.VALIDATION_STATUS_URI_SUFFIX;

@Service
public class LimitedPartnerService {

    private final LimitedPartnerRepository repository;
    private final LimitedPartnerMapper mapper;
    private final LimitedPartnerValidator limitedPartnerValidator;
    private final TransactionService transactionService;
    private final TransactionUtils transactionUtils;

    public LimitedPartnerService(LimitedPartnerRepository repository,
                                 LimitedPartnerMapper mapper,
                                 LimitedPartnerValidator limitedPartnerValidator,
                                 TransactionService transactionService,
                                 TransactionUtils transactionUtils
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.limitedPartnerValidator = limitedPartnerValidator;
        this.transactionService = transactionService;
        this.transactionUtils = transactionUtils;
    }

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        limitedPartnerValidator.validatePartial(limitedPartnerDto);

        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        LimitedPartnerDao insertedSubmission = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String submissionUri = linkAndSaveDao(transaction, insertedSubmission.getId(), dao);
        updateTransactionWithLinksForLimitedPartner(requestId, transaction, submissionUri);

        return insertedSubmission.getId();
    }

    private LimitedPartnerDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, LimitedPartnerDao dao) {
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedAt(LocalDateTime.now());
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());
        dao.setUpdatedAt(LocalDateTime.now());
        dao.setUpdatedBy(userId);

        LimitedPartnerDao insertedSubmission = repository.insert(dao);
        ApiLogger.infoContext(requestId, String.format("Limited Partner submission created with id: %s", insertedSubmission.getId()));
        return insertedSubmission;
    }

    private String linkAndSaveDao(Transaction transaction, String submissionId, LimitedPartnerDao dao) {
        var submissionUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), submissionId);
        dao.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        repository.save(dao);
        return submissionUri;
    }

    private void updateTransactionWithLinksForLimitedPartner(String requestID,
                                                             Transaction transaction,
                                                             String submissionUri)
            throws ServiceException {
        var limitedPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        linksMap.put("validation_status", submissionUri + VALIDATION_STATUS_URI_SUFFIX);
        linksMap.put("costs", submissionUri + "/costs");

        limitedPartnerResource.setLinks(linksMap);
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);

        transaction.setResources(Collections.singletonMap(submissionUri, limitedPartnerResource));

        transactionService.updateTransaction(transaction, requestID);
    }

    public void updateLimitedPartner(Transaction transaction, String limitedPartnerId, LimitedPartnerDataDto limitedPartnerChangesDataDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        checkLimitedPartnerIsLinkedToTransaction(transaction, limitedPartnerId);

        var limitedPartnerDaoBeforePatch = repository.findById(limitedPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", limitedPartnerId)));

        var limitedPartnerDto = mapper.daoToDto(limitedPartnerDaoBeforePatch);

        mapper.update(limitedPartnerChangesDataDto, limitedPartnerDto.getData());

        limitedPartnerValidator.validateUpdate(limitedPartnerDto);

        isSecondNationalityDifferent(limitedPartnerDto);
        handleSecondNationalityOptionality(limitedPartnerChangesDataDto, limitedPartnerDto.getData());

        var limitedPartnerDaoAfterPatch = mapper.dtoToDao(limitedPartnerDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(limitedPartnerDaoBeforePatch, limitedPartnerDaoAfterPatch);

        setAuditDetailsForUpdate(userId, limitedPartnerDaoAfterPatch);

        ApiLogger.infoContext(requestId, String.format("Limited Partner updated with id: %s", limitedPartnerId));

        repository.save(limitedPartnerDaoAfterPatch);
    }

    public LimitedPartnerDto getLimitedPartner(Transaction transaction, String limitedPartnerId) throws ResourceNotFoundException {
        checkLimitedPartnerIsLinkedToTransaction(transaction, limitedPartnerId);

        var limitedPartnerDao = repository.findById(limitedPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Limited partner submission with id %s not found", limitedPartnerId)));
        return mapper.daoToDto(limitedPartnerDao);
    }

    public List<LimitedPartnerDataDto> getLimitedPartnerDataList(Transaction transaction) {
        return repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .map(LimitedPartnerDto::getData)
                .toList();
    }

    public List<LimitedPartnerDto> getLimitedPartnerList(Transaction transaction) {
        return repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .toList();
    }

    public void deleteLimitedPartner(Transaction transaction, String limitedPartnerId, String requestId) throws ServiceException {
        checkLimitedPartnerIsLinkedToTransaction(transaction, limitedPartnerId);

        LimitedPartnerDao limitedPartnerDao = repository.findById(limitedPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Limited partner with id %s not found", limitedPartnerId)));

        repository.deleteById(limitedPartnerDao.getId());

        var resources = transaction.getResources();

        var submissionUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), limitedPartnerId);

        resources.remove(submissionUri);

        transactionService.updateTransaction(transaction, requestId);

        ApiLogger.infoContext(requestId, String.format("Limited Partner deleted with id: %s", limitedPartnerId));

    }

    public List<ValidationStatusError> validateLimitedPartner(Transaction transaction, String limitedPartnerId)
            throws ServiceException {
        LimitedPartnerDto dto = getLimitedPartner(transaction, limitedPartnerId);

        return limitedPartnerValidator.validateFull(dto);
    }

    private void isSecondNationalityDifferent(LimitedPartnerDto limitedPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());
        limitedPartnerValidator.isSecondNationalityDifferent(limitedPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void handleSecondNationalityOptionality(LimitedPartnerDataDto limitedPartnerChangesDataDto,
                                                    LimitedPartnerDataDto limitedPartnerDataDto) {
        // The first 'not null' check here ensures that second nationality isn't wiped if, for example, only address data is being updated
        if (limitedPartnerChangesDataDto.getNationality1() != null && limitedPartnerChangesDataDto.getNationality2() == null) {
            limitedPartnerDataDto.setNationality2(null);
        }
    }

    private void copyMetaDataForUpdate(LimitedPartnerDao limitedPartnerDaoBeforePatch,
                                       LimitedPartnerDao limitedPartnerDaoAfterPatch) {
        limitedPartnerDaoAfterPatch.setId(limitedPartnerDaoBeforePatch.getId());
        limitedPartnerDaoAfterPatch.setCreatedAt(limitedPartnerDaoBeforePatch.getCreatedAt());
        limitedPartnerDaoAfterPatch.setCreatedBy(limitedPartnerDaoBeforePatch.getCreatedBy());
        limitedPartnerDaoAfterPatch.setLinks(limitedPartnerDaoBeforePatch.getLinks());
        limitedPartnerDaoAfterPatch.setTransactionId(limitedPartnerDaoBeforePatch.getTransactionId());
    }

    private void setAuditDetailsForUpdate(String userId, LimitedPartnerDao limitedPartnerDaoAfterPatch) {
        limitedPartnerDaoAfterPatch.setUpdatedAt(LocalDateTime.now());
        limitedPartnerDaoAfterPatch.setUpdatedBy(userId);
    }

    private void checkLimitedPartnerIsLinkedToTransaction(Transaction transaction, String limitedPartnerId) throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        var submissionUri = String.format(URL_GET_LIMITED_PARTNER, transactionId, limitedPartnerId);

        if (!transactionUtils.isTransactionLinkedToPartnerSubmission(transaction, submissionUri, FILING_KIND_LIMITED_PARTNER)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches limited partner id: %s", transactionId, limitedPartnerId));
        }
    }
}
