package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDtoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PersonWithSignificantControlMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.PersonWithSignificantControlValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.PersonWithSignificantControlValidatorStrategy;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder.TRANSACTION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
class PersonWithSignificantControlServiceTest {

    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String PSC_ID = "psc123";

    private static final Transaction TRANSACTION = new TransactionBuilder().withKindAndUri(
                    FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                    URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL,
                    PSC_ID
            )
            .build();
    private static final String FORENAME = "John";
    private static final String SURNAME = "Smith";
    private static final String NATIONALITY_1 = "British";
    private static final String NATIONALITY_2 = "French";

    @InjectMocks
    private PersonWithSignificantControlService personWithSignificantControlService;

    @Mock
    private PersonWithSignificantControlRepository repository;

    @Mock
    private PersonWithSignificantControlMapper mapper;

    @Mock
    private TransactionService transactionService;
    
    @Mock
    private PersonWithSignificantControlValidator personWithSignificantControlValidator;
    
    @Mock
    private PersonWithSignificantControlValidatorStrategy personWithSignificantControlValidatorStrategy;

    @Captor
    private ArgumentCaptor<PersonWithSignificantControlDao> pscDaoArgumentCaptor;

    @Captor
    private ArgumentCaptor<PersonWithSignificantControlDto> pscDtoCaptor;

    private PersonWithSignificantControlDao individualPscDao;
    private PersonWithSignificantControlDto individualPscDto;

    @BeforeEach
    void init() {
        individualPscDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withTransactionId(TRANSACTION_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withForename(FORENAME)
                        .withSurname(SURNAME)
                        .withNationality1(NATIONALITY_1)
                        .withNationality2(NATIONALITY_2)
                        .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                        .build())
                .build();

       individualPscDto = new PersonWithSignificantControlDtoBuilder()
               .withData( new PersonWithSignificantControlDtoBuilder.DataBuilder()
                       .withForename(FORENAME)
                       .withSurname(SURNAME)
                       .withNationality1(Nationality.BRITISH)
                       .withNationality2(Nationality.FRENCH)
                       .withType(PersonWithSignificantControlType.INDIVIDUAL_PERSON)
                       .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                       .build())
               .build();
    }

    @Test
    void testGetPersonWithSignificantControlSuccess() throws ServiceException {
        when(repository.findById(PSC_ID))
                .thenReturn(Optional.of(individualPscDao));

        when(mapper.daoToDto(individualPscDao)).thenReturn(individualPscDto);
        when(transactionService.isTransactionLinkedToResource(any(), anyString(), anyString()))
                .thenReturn(true);

        var dto = personWithSignificantControlService.getPersonWithSignificantControl(TRANSACTION, PSC_ID);
        assertEquals(FORENAME, dto.getData().getForename());
        assertEquals(SURNAME, dto.getData().getSurname());
    }

    @Test
    void testGetPersonWithSignificantControlNotFound() {
        when(repository.findById(PSC_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> personWithSignificantControlService.getPersonWithSignificantControl(TRANSACTION, PSC_ID));
        assertEquals("Person with significant control resource with id " + PSC_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testGetPscTransactionLinkedToPersonWithSignificantControlFails() {
        when(repository.findById(PSC_ID))
                .thenReturn(Optional.of(individualPscDao));

        when(transactionService.isTransactionLinkedToResource(eq(TRANSACTION), any(String.class), any(String.class)))
                .thenReturn(false);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> personWithSignificantControlService.getPersonWithSignificantControl(TRANSACTION, PSC_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testCreatePersonWithSignificantControlReturnsSuccess() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var submissionUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(personWithSignificantControlValidator.getValidatorByType(any(PersonWithSignificantControlType.class))).thenReturn(personWithSignificantControlValidatorStrategy);
        when(mapper.dtoToDao(individualPscDto)).thenReturn(individualPscDao);
        when(repository.insert(individualPscDao)).thenReturn(individualPscDao);

        String submissionId = personWithSignificantControlService.createPersonWithSignificantControl(TRANSACTION, individualPscDto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(individualPscDto);
        verify(repository, times(1)).insert(individualPscDao);
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());
        verify(transactionService, times(1)).updateTransactionWithLinksForResource(REQUEST_ID, TRANSACTION, submissionUri, individualPscDao.getData().getKind(), null);

        PersonWithSignificantControlDao sentSubmission = pscDaoArgumentCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL, sentSubmission.getData().getKind());
        assertEquals(PSC_ID, submissionId);

        // Assert self link
        String expectedUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void testCreatePscWithNullKindDefaultsToPersonWithSignificantControl() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        individualPscDao.getData().setKind(null);

        when(personWithSignificantControlValidator.getValidatorByType(any(PersonWithSignificantControlType.class))).thenReturn(personWithSignificantControlValidatorStrategy);
        when(mapper.dtoToDao(individualPscDto)).thenReturn(individualPscDao);
        when(repository.insert(individualPscDao)).thenReturn(individualPscDao);

        personWithSignificantControlService.createPersonWithSignificantControl(TRANSACTION, individualPscDto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(individualPscDto);
        verify(repository, times(1)).insert(individualPscDao);
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao sentSubmission = pscDaoArgumentCaptor.getValue();
        assertEquals(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL, sentSubmission.getData().getKind());
    }

    @Test
    void testUpdatePersonWithSignificantControlPersistsUpdatedFieldsSuccessfully() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        PersonWithSignificantControlDataDto changesDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withForename("Updated Forename")
                .withSurname("Updated Surname")
                .build();
        PersonWithSignificantControlDao afterPatchDao = new PersonWithSignificantControlDao();

        when(personWithSignificantControlValidator.getValidatorByType(any(PersonWithSignificantControlType.class))).thenReturn(personWithSignificantControlValidatorStrategy);
        when(repository.findById(PSC_ID)).thenReturn(Optional.of(individualPscDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(true);
        when(mapper.daoToDto(individualPscDao)).thenReturn(individualPscDto);
        when(mapper.dtoToDao(individualPscDto)).thenReturn(afterPatchDao);

        assertNull(afterPatchDao.getId());
        assertNull(afterPatchDao.getCreatedAt());
        assertNull(afterPatchDao.getCreatedBy());
        assertNull(afterPatchDao.getLinks());
        assertNull(afterPatchDao.getTransactionId());

        personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, changesDataDto, REQUEST_ID, USER_ID);

        verify(repository, times(1)).findById(PSC_ID);
        verify(mapper, times(1)).daoToDto(individualPscDao);
        verify(mapper, times(1)).update(changesDataDto, individualPscDto.getData());
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedDao = pscDaoArgumentCaptor.getValue();
        assertEquals(individualPscDao.getId(), savedDao.getId());
        assertEquals(individualPscDao.getCreatedAt(), savedDao.getCreatedAt());
        assertEquals(individualPscDao.getCreatedBy(), savedDao.getCreatedBy());
        assertEquals(individualPscDao.getLinks(), savedDao.getLinks());
        assertEquals(individualPscDao.getTransactionId(), savedDao.getTransactionId());
        assertEquals(USER_ID, savedDao.getUpdatedBy());
    }

    @Test
    void testUpdatePersonWithSignificantControlRemovesSecondNationalityWhenPatchedToNull() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        PersonWithSignificantControlDataDto changesDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withForename("Updated Forename")
                .withSurname("Updated Surname")
                .withNationality1(Nationality.BRITISH)
                .withNationality2(null)
                .build();
        PersonWithSignificantControlDao afterPatchDao = new PersonWithSignificantControlDao();

        when(personWithSignificantControlValidator.getValidatorByType(any(PersonWithSignificantControlType.class))).thenReturn(personWithSignificantControlValidatorStrategy);
        when(repository.findById(PSC_ID)).thenReturn(Optional.of(individualPscDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(true);
        when(mapper.daoToDto(individualPscDao)).thenReturn(individualPscDto);
        when(mapper.dtoToDao(individualPscDto)).thenReturn(afterPatchDao);

        assertNotNull(individualPscDto.getData().getNationality2());

        personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, changesDataDto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).update(changesDataDto, individualPscDto.getData());
        verify(mapper, times(1)).dtoToDao(pscDtoCaptor.capture());

        var capturedDto = pscDtoCaptor.getValue();
        assertNull(capturedDto.getData().getNationality2());
    }

    @Test
    void testUpdatePersonWithSignificantControlNotFound() {
        when(repository.findById(PSC_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, new PersonWithSignificantControlDataDto(), REQUEST_ID, USER_ID));
        assertEquals("Person with significant control with id " + PSC_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testUpdatePersonWithSignificantControlTransactionNotLinked() {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(individualPscDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, new PersonWithSignificantControlDataDto(), REQUEST_ID, USER_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testDeletePersonWithSignificantControlSuccess() throws ServiceException {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(individualPscDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(true);

        personWithSignificantControlService.deletePersonWithSignificantControl(TRANSACTION, PSC_ID, REQUEST_ID);

        verify(repository, times(1)).findById(PSC_ID);
        verify(transactionService, times(1)).isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        verify(transactionService, times(1)).deleteTransactionResource(TRANSACTION.getId(), pscUri, REQUEST_ID);
        verify(repository, times(1)).deleteById(PSC_ID);
    }

    @Test
    void testDeletePersonWithSignificantControlNotFound() {
        when(repository.findById(PSC_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> personWithSignificantControlService.deletePersonWithSignificantControl(TRANSACTION, PSC_ID, REQUEST_ID));
        assertEquals("Person with significant control with id " + PSC_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testDeletePersonWithSignificantControlTransactionNotLinked() {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(individualPscDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> personWithSignificantControlService.deletePersonWithSignificantControl(TRANSACTION, PSC_ID, REQUEST_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }
}
