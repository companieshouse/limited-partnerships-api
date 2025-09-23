package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.GeneralPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
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

        generalPartnerValidator.validatePartial(generalPartnerDto, transaction);

        GeneralPartnerDao dao = mapper.dtoToDao(generalPartnerDto);
        GeneralPartnerDao insertedSubmission = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String submissionUri = linkAndSaveDao(transaction, insertedSubmission.getId(), dao);

        String kind = requireNonNullElse(insertedSubmission.getData().getKind(), FILING_KIND_LIMITED_PARTNERSHIP);

        transactionService.updateTransactionWithLinksForPartner(requestId, transaction, submissionUri, kind);

        return insertedSubmission.getId();
    }

    private GeneralPartnerDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, GeneralPartnerDao dao) {
        if (dao.getData().getKind() == null) {
            dao.getData().setKind(FILING_KIND_GENERAL_PARTNER);
        }

        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedBy(userId);
        dao.setUpdatedBy(userId);
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

    public void updateGeneralPartner(Transaction transaction, String generalPartnerId, GeneralPartnerDataDto generalPartnerChangesDataDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var generalPartnerDaoBeforePatch = repository.findById(generalPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", generalPartnerId)));

        String kind = requireNonNullElse(generalPartnerDaoBeforePatch.getData().getKind(), FILING_KIND_GENERAL_PARTNER);

        checkGeneralPartnerIsLinkedToTransaction(transaction, generalPartnerId, kind);

        var generalPartnerDto = mapper.daoToDto(generalPartnerDaoBeforePatch);

        mapper.update(generalPartnerChangesDataDto, generalPartnerDto.getData());

        generalPartnerValidator.validateUpdate(generalPartnerDto, transaction);

        handleSecondNationalityOptionality(generalPartnerChangesDataDto, generalPartnerDto.getData());

        var generalPartnerDaoAfterPatch = mapper.dtoToDao(generalPartnerDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(generalPartnerDaoBeforePatch, generalPartnerDaoAfterPatch);

        setAuditDetailsForUpdate(userId, generalPartnerDaoAfterPatch);

        ApiLogger.infoContext(requestId, String.format("General Partner updated with id: %s", generalPartnerId));

        repository.save(generalPartnerDaoAfterPatch);
    }

    public GeneralPartnerDto getGeneralPartner(Transaction transaction, String generalPartnerId) throws ResourceNotFoundException {

        var generalPartnerDao = repository.findById(generalPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("General partner submission with id %s not found", generalPartnerId)));

        String kind = requireNonNullElse(generalPartnerDao.getData().getKind(), FILING_KIND_GENERAL_PARTNER);

        checkGeneralPartnerIsLinkedToTransaction(transaction, generalPartnerId, kind);

        return mapper.daoToDto(generalPartnerDao);
    }

    public List<ValidationStatusError> validateGeneralPartner(Transaction transaction, String generalPartnerId)
            throws ServiceException {
        GeneralPartnerDto dto = getGeneralPartner(transaction, generalPartnerId);

        return generalPartnerValidator.validateFull(dto, transaction);
    }

    public List<ValidationStatusError> validateGeneralPartners(Transaction transaction) throws ServiceException {
        List<GeneralPartnerDto> generalPartners = repository.findAllByTransactionIdOrderByUpdatedAtDesc(
                transaction.getId()).stream().map(mapper::daoToDto).toList();

        List<ValidationStatusError> errors = new ArrayList<>();

        if (generalPartners.isEmpty()) {
            errors.add(new ValidationStatusError("At least one general partner is required", "general_partners", null, null));

            return errors;
        }

        for (GeneralPartnerDto partner : generalPartners) {
            errors.addAll(generalPartnerValidator.validateFull(partner, transaction));
        }

        return errors;
    }

    public List<GeneralPartnerDto> getGeneralPartnerList(Transaction transaction) throws ServiceException {
        List<GeneralPartnerDto> generalPartnerDtos = repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto).toList();

        for (GeneralPartnerDto generalPartnerDto : generalPartnerDtos) {
            boolean isCompleted = generalPartnerValidator.validateFull(generalPartnerDto, transaction).isEmpty();
            generalPartnerDto.getData().setCompleted(isCompleted);
        }

        return generalPartnerDtos;
    }

    public List<GeneralPartnerDataDto> getGeneralPartnerDataList(Transaction transaction) {
        return repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .map(GeneralPartnerDto::getData)
                .toList();
    }

    public void deleteGeneralPartner(Transaction transaction, String generalPartnerId, String requestId) throws ServiceException {
        GeneralPartnerDao generalPartnerDao = repository.findById(generalPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("General partner with id %s not found", generalPartnerId)));

        String kind = requireNonNullElse(generalPartnerDao.getData().getKind(), FILING_KIND_GENERAL_PARTNER);

        checkGeneralPartnerIsLinkedToTransaction(transaction, generalPartnerId, kind);

        var submissionUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), generalPartnerId);

        transactionService.deleteTransactionResource(transaction.getId(), submissionUri, requestId);
        repository.deleteById(generalPartnerDao.getId());

        ApiLogger.infoContext(requestId, String.format("General Partner deleted with id: %s", generalPartnerId));
    }

    private void handleSecondNationalityOptionality(GeneralPartnerDataDto generalPartnerChangesDataDto,
                                                    GeneralPartnerDataDto generalPartnerDataDto) {
        // The first 'not null' check here ensures that second nationality isn't wiped if, for example, only address data is being updated
        if (generalPartnerChangesDataDto.getNationality1() != null && generalPartnerChangesDataDto.getNationality2() == null) {
            generalPartnerDataDto.setNationality2(null);
        }
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
        generalPartnerDaoAfterPatch.setUpdatedBy(userId);
    }

    private void checkGeneralPartnerIsLinkedToTransaction(Transaction transaction, String generalPartnerId, String kind) throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        var submissionUri = String.format(URL_GET_GENERAL_PARTNER, transactionId, generalPartnerId);

        if (!transactionService.isTransactionLinkedToPartner(transaction, submissionUri, kind)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches general partner id: %s", transactionId, generalPartnerId));
        }
    }
}

