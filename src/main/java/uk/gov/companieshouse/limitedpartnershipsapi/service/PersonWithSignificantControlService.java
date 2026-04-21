package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PersonWithSignificantControlMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.PersonWithSignificantControlValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.NationalityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.MetaDataUtils.copyMetaDataForPatch;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.MetaDataUtils.setAuditDetailsForPatch;

@Service
public class PersonWithSignificantControlService {

    private final PersonWithSignificantControlRepository repository;
    private final PersonWithSignificantControlMapper mapper;
    private final TransactionService transactionService;
    private final PersonWithSignificantControlValidator personWithSignificantControlValidator;

    public PersonWithSignificantControlService(PersonWithSignificantControlRepository repository,
                                               PersonWithSignificantControlMapper mapper,
                                               TransactionService transactionService,
                                               PersonWithSignificantControlValidator personWithSignificantControlValidator
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionService = transactionService;
        this.personWithSignificantControlValidator = personWithSignificantControlValidator;
    }

    public PersonWithSignificantControlDto getPersonWithSignificantControl(Transaction transaction, String personWithSignificantControlId) throws ResourceNotFoundException {

        var personWithSignificantControlDao = repository.findById(personWithSignificantControlId).orElseThrow(() -> new ResourceNotFoundException(String.format("Person with significant control resource with id %s not found", personWithSignificantControlId)));

        String kind = requireNonNullElse(personWithSignificantControlDao.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);

        checkPersonWithSignificantControlIsLinkedToTransaction(transaction, personWithSignificantControlId, kind);

        return mapper.daoToDto(personWithSignificantControlDao);
    }

    public String createPersonWithSignificantControl(Transaction transaction, PersonWithSignificantControlDto personWithSignificantControlDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var validator = personWithSignificantControlValidator.getValidatorByType(personWithSignificantControlDto.getData().getType());
        validator.validatePartial(personWithSignificantControlDto);

        PersonWithSignificantControlDao dao = mapper.dtoToDao(personWithSignificantControlDto);
        PersonWithSignificantControlDao insertedResource = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String resourceUri = linkAndSaveDao(transaction, insertedResource.getId(), dao);

        String kind = requireNonNullElse(insertedResource.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);

        transactionService.updateTransactionWithLinksForResource(requestId, transaction, resourceUri, kind, null);

        return insertedResource.getId();
    }

    public void updatePersonWithSignificantControl(Transaction transaction, String personWithSignificantControlId, PersonWithSignificantControlDataDto personWithSignificantControlChangesDataDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var daoBeforePatch = repository.findById(personWithSignificantControlId).orElseThrow(() -> new ResourceNotFoundException(String.format("Person with significant control with id %s not found", personWithSignificantControlId)));
        String kind = requireNonNullElse(daoBeforePatch.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        checkPersonWithSignificantControlIsLinkedToTransaction(transaction, personWithSignificantControlId, kind);

        var dto = mapper.daoToDto(daoBeforePatch);
        mapper.update(personWithSignificantControlChangesDataDto, dto.getData());

        var validator = personWithSignificantControlValidator.getValidatorByType(dto.getData().getType());
        validator.validatePartial(dto);

        NationalityUtils.handleSecondNationalityOptionality(personWithSignificantControlChangesDataDto, dto.getData());
        handleLegalEntityRegistrationLocationOptionality(personWithSignificantControlChangesDataDto, dto.getData());

        var daoAfterPatch = mapper.dtoToDao(dto);
        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForPatch(daoBeforePatch, daoAfterPatch);
        setAuditDetailsForPatch(daoAfterPatch, userId);
        ApiLogger.infoContext(requestId, String.format("Person with significant control updated with id: %s", personWithSignificantControlId));

        repository.save(daoAfterPatch);
    }

    private void handleLegalEntityRegistrationLocationOptionality(PersonWithSignificantControlDataDto personWithSignificantControlChangesDataDto, PersonWithSignificantControlDataDto data) {
        if (personWithSignificantControlChangesDataDto.getLegalEntityRegistrationLocation() == null) {
            data.setLegalEntityRegistrationLocation(null);
        }
    }

    public void deletePersonWithSignificantControl(Transaction transaction, String personWithSignificantControlId, String requestId) throws ServiceException {
        PersonWithSignificantControlDao personWithSignificantControlDao = repository.findById(personWithSignificantControlId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Person with significant control with id %s not found", personWithSignificantControlId)));

        String kind = requireNonNullElse(personWithSignificantControlDao.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);

        checkPersonWithSignificantControlIsLinkedToTransaction(transaction, personWithSignificantControlId, kind);

        var personWithSignificantControlUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, transaction.getId(), personWithSignificantControlId);

        transactionService.deleteTransactionResource(transaction.getId(), personWithSignificantControlUri, requestId);
        repository.deleteById(personWithSignificantControlDao.getId());

        ApiLogger.infoContext(requestId, String.format("Person with significant control deleted with id: %s", personWithSignificantControlId));
    }

    public List<ValidationStatusError> validatePersonsWithSignificantControl(Transaction transaction) throws ServiceException {
        List<PersonWithSignificantControlDto> personsWithSignificantControl = repository.findAllByTransactionIdOrderByUpdatedAtDesc(
                transaction.getId()).stream().map(mapper::daoToDto).toList();

        // TODO check if list is not empty when not doing a Registration?
        // TODO check statement?

        

        List<ValidationStatusError> errors = new ArrayList<>();

        for (PersonWithSignificantControlDto personWithSignificantControlDto: personsWithSignificantControl) {
            var validator = personWithSignificantControlValidator.getValidatorByType(personWithSignificantControlDto.getData().getType());
            errors.addAll(validator.validateFull(personWithSignificantControlDto));
        }

        return errors;
    }

    private PersonWithSignificantControlDao insertDaoWithMetadata(
            String requestId, Transaction transaction, String userId, PersonWithSignificantControlDao dao) {
        if (dao.getData().getKind() == null) {
            dao.getData().setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        }

        dao.getData().setEtag(GenerateEtagUtil.generateEtag());
        dao.setCreatedBy(userId);
        dao.setUpdatedBy(userId);
        dao.setTransactionId(transaction.getId());

        PersonWithSignificantControlDao insertedResource = repository.insert(dao);
        ApiLogger.infoContext(requestId, String.format("Person with significant control resource created with id: %s", insertedResource.getId()));
        return insertedResource;
    }

    private String linkAndSaveDao(Transaction transaction, String resourceId, PersonWithSignificantControlDao dao) {
        var resourceUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, transaction.getId(), resourceId);
        dao.setLinks(Collections.singletonMap(LINK_SELF, resourceUri));
        repository.save(dao);
        return resourceUri;
    }

    private void checkPersonWithSignificantControlIsLinkedToTransaction(Transaction transaction, String personWithSignificantControlId, String kind) throws ResourceNotFoundException {
        String transactionId = transaction.getId();
        var resourceUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, transactionId, personWithSignificantControlId);

        if (!transactionService.isTransactionLinkedToResource(transaction, resourceUri, kind)) {
            throw new ResourceNotFoundException(String.format(
                    "Transaction id: %s does not have a resource that matches person with significant control id: %s", transactionId, personWithSignificantControlId));
        }
    }
}
