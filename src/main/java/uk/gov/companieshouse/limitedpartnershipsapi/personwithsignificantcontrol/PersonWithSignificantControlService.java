package uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.enums.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.NationalityUtils;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.personwithsignificantcontrol.PersonWithSignificantControlValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.MetaDataUtils.copyMetaDataForPatch;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.MetaDataUtils.setAuditDetailsForPatch;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.Operation.DELETION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.Operation.INSERTION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.executeWithTransactionalRollback;

@Service
public class PersonWithSignificantControlService {

    private final PersonWithSignificantControlRepository repository;
    private final PersonWithSignificantControlMapper mapper;
    private final TransactionService transactionService;
    private final PersonWithSignificantControlValidator personWithSignificantControlValidator;
    private final PartnershipService partnershipService;

    public PersonWithSignificantControlService(PersonWithSignificantControlRepository repository,
                                               PersonWithSignificantControlMapper mapper,
                                               TransactionService transactionService,
                                               PersonWithSignificantControlValidator personWithSignificantControlValidator,
                                               PartnershipService partnershipService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.transactionService = transactionService;
        this.personWithSignificantControlValidator = personWithSignificantControlValidator;
        this.partnershipService = partnershipService;
    }

    public PersonWithSignificantControlDto getPersonWithSignificantControl(Transaction transaction, String personWithSignificantControlId) throws ResourceNotFoundException {

        var personWithSignificantControlDao = repository.findById(personWithSignificantControlId).orElseThrow(() -> new ResourceNotFoundException(String.format("Person with significant control resource with id %s not found", personWithSignificantControlId)));

        String kind = requireNonNullElse(personWithSignificantControlDao.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);

        checkPersonWithSignificantControlIsLinkedToTransaction(transaction, personWithSignificantControlId, kind);

        return mapper.daoToDto(personWithSignificantControlDao);
    }

    public List<PersonWithSignificantControlDto> getPersonWithSignificantControlList(Transaction transaction) throws ServiceException {
        List<PersonWithSignificantControlDto> personWithSignificantControlDtos = repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .toList();

        for (PersonWithSignificantControlDto personWithSignificantControlDto : personWithSignificantControlDtos) {
            boolean isCompleted = personWithSignificantControlValidator.getValidatorByType(personWithSignificantControlDto.getData().getType())
                    .validateFull(personWithSignificantControlDto)
                    .isEmpty();
            personWithSignificantControlDto.getData().setCompleted(isCompleted);
        }

        return personWithSignificantControlDtos;
    }

    public List<PersonWithSignificantControlDataDto> getPersonWithSignificantControlDataList(Transaction transaction) {
        return repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId()).stream()
                .map(mapper::daoToDto)
                .map(PersonWithSignificantControlDto::getData)
                .toList();
    }

    public String createPersonWithSignificantControl(Transaction transaction, PersonWithSignificantControlDto personWithSignificantControlDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var validator = personWithSignificantControlValidator.getValidatorByType(personWithSignificantControlDto.getData().getType());
        validator.validatePartial(personWithSignificantControlDto);

        PersonWithSignificantControlDao dao = mapper.dtoToDao(personWithSignificantControlDto);
        PersonWithSignificantControlDao insertedResource = insertDaoWithMetadata(requestId, transaction, userId, dao);
        String resourceUri = linkAndSaveDao(transaction, insertedResource.getId(), dao);

        String kind = requireNonNullElse(insertedResource.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);

        executeWithTransactionalRollback(
            requestId,
            insertedResource.getId(),
            () -> transactionService.updateTransactionWithLinksForResource(requestId, transaction, resourceUri, kind, null),
            INSERTION,
            () -> repository.deleteById(insertedResource.getId())
        );

        return insertedResource.getId();
    }

    public void updatePersonWithSignificantControl(Transaction transaction, String personWithSignificantControlId, PersonWithSignificantControlDataDto personWithSignificantControlChangesDataDto, String requestId, String userId) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var daoBeforePatch = repository.findById(personWithSignificantControlId).orElseThrow(() -> new ResourceNotFoundException(String.format("Person with significant control with id %s not found", personWithSignificantControlId)));
        String kind = requireNonNullElse(daoBeforePatch.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        checkPersonWithSignificantControlIsLinkedToTransaction(transaction, personWithSignificantControlId, kind);

        var dto = mapper.daoToDto(daoBeforePatch);

        var validator = personWithSignificantControlValidator.getValidatorByType(dto.getData().getType());
        mapper.update(personWithSignificantControlChangesDataDto, dto.getData());
        // do this before validator to ensure that if the legalEntityRegistrationLocation is not present in the patch, it is set to null in the DTO before validation
        handleLegalEntityRegistrationLocationOptionality(personWithSignificantControlChangesDataDto, dto.getData());

        validator.validatePartial(dto);

        NationalityUtils.handleSecondNationalityOptionality(personWithSignificantControlChangesDataDto, dto.getData());
        handlePersonOptionalFields(personWithSignificantControlChangesDataDto, dto.getData());

        var daoAfterPatch = mapper.dtoToDao(dto);
        // Need to ensure we don't lose the meta-data already set on the Mongo document (but lost when DAO is mapped to a DTO)
        copyMetaDataForPatch(daoBeforePatch, daoAfterPatch);
        setAuditDetailsForPatch(daoAfterPatch, userId);
        ApiLogger.infoContext(requestId, String.format("Person with significant control updated with id: %s", personWithSignificantControlId));

        repository.save(daoAfterPatch);
    }

    private void handleLegalEntityRegistrationLocationOptionality(PersonWithSignificantControlDataDto changesDataDto, PersonWithSignificantControlDataDto data) {
        if (!PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY.equals(changesDataDto.getType())) {
            return;
        }

        if (changesDataDto.getLegalEntityRegistrationLocation() == null) {
            data.setLegalEntityRegistrationLocation(null);
        }
    }

    private void handlePersonOptionalFields(PersonWithSignificantControlDataDto changesDto, PersonWithSignificantControlDataDto data) {
        if (changesDto.getForename() != null && changesDto.getSurname() != null) {
            if (changesDto.getMiddleNames() == null) {
                data.setMiddleNames(null);
            }
            if (changesDto.getTitle() == null) {
                data.setTitle(null);
            }
        }
    }


    public void deletePersonWithSignificantControl(Transaction transaction, String personWithSignificantControlId, String requestId) throws ServiceException {
        PersonWithSignificantControlDao personWithSignificantControlDao = repository.findById(personWithSignificantControlId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Person with significant control with id %s not found", personWithSignificantControlId)));

        String kind = requireNonNullElse(personWithSignificantControlDao.getData().getKind(), FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);

        checkPersonWithSignificantControlIsLinkedToTransaction(transaction, personWithSignificantControlId, kind);

        var personWithSignificantControlUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, transaction.getId(), personWithSignificantControlId);

        repository.deleteById(personWithSignificantControlDao.getId());

        executeWithTransactionalRollback(
            requestId,
            personWithSignificantControlId,
            () -> transactionService.deleteTransactionResource(transaction.getId(), personWithSignificantControlUri, requestId),
            DELETION,
            () -> repository.save(personWithSignificantControlDao)
        );

        ApiLogger.infoContext(requestId, String.format("Person with significant control deleted with id: %s", personWithSignificantControlId));
        
        // if there are no more persons with significant control, update the partnership to reflect this
        if (repository.countByTransactionId(transaction.getId()) == 0) {
            partnershipService.clearHasPersonWithSignificantControl(transaction, requestId);
        }
    }

    public List<ValidationStatusError> validatePersonsWithSignificantControl(Transaction transaction) throws ServiceException {
        List<PersonWithSignificantControlDto> personsWithSignificantControl = repository.findAllByTransactionIdOrderByUpdatedAtDesc(
                transaction.getId()).stream().map(mapper::daoToDto).toList();

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
