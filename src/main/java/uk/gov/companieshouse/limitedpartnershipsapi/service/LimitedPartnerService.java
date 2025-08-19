package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@Service
public class LimitedPartnerService {

    private final LimitedPartnerRepository repository;
    private final LimitedPartnerMapper mapper;
    private final LimitedPartnerValidator limitedPartnerValidator;
    private final TransactionService transactionService;

    public LimitedPartnerService(LimitedPartnerRepository repository,
                                 LimitedPartnerMapper mapper,
                                 LimitedPartnerValidator limitedPartnerValidator,
                                 TransactionService transactionService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.limitedPartnerValidator = limitedPartnerValidator;
        this.transactionService = transactionService;
    }

    public String createLimitedPartner(Transaction transaction, LimitedPartnerDto limitedPartnerDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        limitedPartnerValidator.validatePartial(limitedPartnerDto, transaction);

        LimitedPartnerDao dao = mapper.dtoToDao(limitedPartnerDto);
        LimitedPartnerDao insertedSubmission = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String submissionUri = linkAndSaveDao(transaction, insertedSubmission.getId(), dao);
        transactionService.updateTransactionWithLinksForLimitedPartner(requestId, transaction, submissionUri, insertedSubmission.getData().getKind());
        return insertedSubmission.getId();
    }

    private LimitedPartnerDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, LimitedPartnerDao dao) {
        if (dao.getData().getKind() == null) {
            dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        }

        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedBy(userId);
        dao.setTransactionId(transaction.getId());
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

    public void updateLimitedPartner(Transaction transaction, String limitedPartnerId, LimitedPartnerDataDto limitedPartnerChangesDataDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var limitedPartnerDaoBeforePatch = repository.findById(limitedPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", limitedPartnerId)));

        checkLimitedPartnerIsLinkedToTransaction(transaction, limitedPartnerId, limitedPartnerDaoBeforePatch.getData().getKind());

        var limitedPartnerDto = mapper.daoToDto(limitedPartnerDaoBeforePatch);

        mapper.update(limitedPartnerChangesDataDto, limitedPartnerDto.getData());

        limitedPartnerValidator.validateUpdate(limitedPartnerDto, limitedPartnerChangesDataDto, transaction);

        handleSecondNationalityOptionality(limitedPartnerChangesDataDto, limitedPartnerDto.getData());

        var limitedPartnerDaoAfterPatch = mapper.dtoToDao(limitedPartnerDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(limitedPartnerDaoBeforePatch, limitedPartnerDaoAfterPatch);

        setAuditDetailsForUpdate(userId, limitedPartnerDaoAfterPatch);

        ApiLogger.infoContext(requestId, String.format("Limited Partner updated with id: %s", limitedPartnerId));

        repository.save(limitedPartnerDaoAfterPatch);
    }

    public LimitedPartnerDto getLimitedPartner(Transaction transaction, String limitedPartnerId) throws ResourceNotFoundException {
        var limitedPartnerDao = repository.findById(limitedPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Limited partner submission with id %s not found", limitedPartnerId)));

        checkLimitedPartnerIsLinkedToTransaction(transaction, limitedPartnerId, limitedPartnerDao.getData().getKind());

        return mapper.daoToDto(limitedPartnerDao);
    }

    public List<LimitedPartnerDataDto> getLimitedPartnerDataList(Transaction transaction) {
        return repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .map(LimitedPartnerDto::getData)
                .toList();
    }

    public List<LimitedPartnerDto> getLimitedPartnerList(Transaction transaction) throws ServiceException {
        List<LimitedPartnerDto> limitedPartnerDtos = repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .toList();

        for (LimitedPartnerDto limitedPartnerDto : limitedPartnerDtos) {
            boolean isCompleted = limitedPartnerValidator.validateFull(limitedPartnerDto, transaction).isEmpty();
            limitedPartnerDto.getData().setCompleted(isCompleted);
        }

        return limitedPartnerDtos;
    }

    public void deleteLimitedPartner(Transaction transaction, String limitedPartnerId, String requestId) throws ServiceException {
        LimitedPartnerDao limitedPartnerDao = repository.findById(limitedPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Limited partner with id %s not found", limitedPartnerId)));

        checkLimitedPartnerIsLinkedToTransaction(transaction, limitedPartnerId, limitedPartnerDao.getData().getKind());

        var submissionUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), limitedPartnerId);

        transactionService.deleteTransactionResource(transaction.getId(), submissionUri, requestId);
        repository.deleteById(limitedPartnerDao.getId());

        ApiLogger.infoContext(requestId, String.format("Limited Partner deleted with id: %s", limitedPartnerId));
    }

    public List<ValidationStatusError> validateLimitedPartner(Transaction transaction, String limitedPartnerId)
            throws ServiceException {
        LimitedPartnerDto dto = getLimitedPartner(transaction, limitedPartnerId);

        return limitedPartnerValidator.validateFull(dto, transaction);
    }

    public List<ValidationStatusError> validateLimitedPartners(Transaction transaction) throws ServiceException {
        List<LimitedPartnerDto> limitedPartners = repository.findAllByTransactionIdOrderByUpdatedAtDesc(
                transaction.getId()).stream().map(mapper::daoToDto).toList();

        List<ValidationStatusError> errors = new ArrayList<>();

        if (limitedPartners.isEmpty()) {
            errors.add(new ValidationStatusError("At least one limited partner is required", "limited_partners", null, null));

            return errors;
        }

        for (LimitedPartnerDto partner : limitedPartners) {
            errors.addAll(limitedPartnerValidator.validateFull(partner, transaction));
        }

        return errors;
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
        limitedPartnerDaoAfterPatch.setUpdatedBy(userId);
    }

    private void checkLimitedPartnerIsLinkedToTransaction(Transaction transaction, String limitedPartnerId, String kind) throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        var submissionUri = String.format(URL_GET_LIMITED_PARTNER, transactionId, limitedPartnerId);

        if (!transactionService.isTransactionLinkedToPartner(transaction, submissionUri, kind)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches limited partner id: %s", transactionId, limitedPartnerId));
        }
    }
}
