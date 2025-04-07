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
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.VALIDATION_STATUS_URI_SUFFIX;

@Service
public class GeneralPartnerService {

    private final GeneralPartnerRepository repository;
    private final GeneralPartnerMapper mapper;
    private final GeneralPartnerValidator generalPartnerValidator;
    private final TransactionService transactionService;
    private final TransactionUtils transactionUtils;

    public GeneralPartnerService(GeneralPartnerRepository repository,
                                 GeneralPartnerMapper mapper,
                                 GeneralPartnerValidator generalPartnerValidator,
                                 TransactionService transactionService,
                                 TransactionUtils transactionUtils
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.generalPartnerValidator = generalPartnerValidator;
        this.transactionService = transactionService;
        this.transactionUtils = transactionUtils;
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
            String requestId, Transaction transaction, String submissionUri) throws ServiceException {
        var generalPartnerResource = new Resource();

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        linksMap.put("validation_status", submissionUri + VALIDATION_STATUS_URI_SUFFIX);

        generalPartnerResource.setLinks(linksMap);
        generalPartnerResource.setKind(FILING_KIND_GENERAL_PARTNER);

        transaction.setResources(Collections.singletonMap(submissionUri, generalPartnerResource));

        transactionService.updateTransaction(transaction, requestId);
    }

    public void updateGeneralPartner(String generalPartnerId, GeneralPartnerDataDto generalPartnerChangesDataDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var generalPartnerDaoBeforePatch = repository.findById(generalPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("Submission with id %s not found", generalPartnerId)));

        var generalPartnerDto = mapper.daoToDto(generalPartnerDaoBeforePatch);

        mapper.update(generalPartnerChangesDataDto, generalPartnerDto.getData());

        isSecondNationalityDifferent(generalPartnerDto);
        handleSecondNationalityOptionality(generalPartnerChangesDataDto, generalPartnerDto.getData());

        var generalPartnerDaoAfterPatch = mapper.dtoToDao(generalPartnerDto);

        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForUpdate(generalPartnerDaoBeforePatch, generalPartnerDaoAfterPatch);

        setAuditDetailsForUpdate(userId, generalPartnerDaoAfterPatch);

        ApiLogger.infoContext(requestId, String.format("General Partner updated with id: %s", generalPartnerId));

        repository.save(generalPartnerDaoAfterPatch);
    }

    public GeneralPartnerDto getGeneralPartner(Transaction transaction, String generalPartnerId) throws ResourceNotFoundException {
        checkGeneralPartnerIsLinkedToPartnership(transaction, generalPartnerId);
        var generalPartnerDao = repository.findById(generalPartnerId).orElseThrow(() -> new ResourceNotFoundException(String.format("General partner submission with id %s not found", generalPartnerId)));
        return mapper.daoToDto(generalPartnerDao);
    }

    public List<ValidationStatusError> validateGeneralPartner(Transaction transaction, String generalPartnerId)
            throws ServiceException {
        GeneralPartnerDto dto = getGeneralPartner(transaction, generalPartnerId);

        return generalPartnerValidator.validate(dto);
    }

    public List<GeneralPartnerDto> getGeneralPartnerList(Transaction transaction) {
        return repository.findAll().stream().
                filter(gp -> gp.getLinks().get(LINK_SELF).contains(transaction.getId())).
                map(mapper::daoToDto).
                collect(Collectors.toList());
    }

    private void isSecondNationalityDifferent(GeneralPartnerDto generalPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());
        generalPartnerValidator.isSecondNationalityDifferent(generalPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
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
        generalPartnerDaoAfterPatch.setUpdatedAt(LocalDateTime.now());
        generalPartnerDaoAfterPatch.setUpdatedBy(userId);
    }

    private void checkGeneralPartnerIsLinkedToPartnership(Transaction transaction, String generalPartnerId) throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        var submissionUri = String.format(URL_GET_GENERAL_PARTNER, transactionId, generalPartnerId);
        if (!transactionUtils.isTransactionLinkedToPartnerSubmission(transaction, submissionUri, FILING_KIND_GENERAL_PARTNER)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches general partner id: %s", transactionId, generalPartnerId));
        }
    }
}

