package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PersonWithSignificantControlMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;

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
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
class PersonWithSignificantControlServiceTest {

    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String PSC_ID = PersonWithSignificantControlBuilder.ID;

    private static final Transaction TRANSACTION = new TransactionBuilder().withKindAndUri(
                    FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                    URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL,
                    PSC_ID
            )
            .build();

    @InjectMocks
    private PersonWithSignificantControlService personWithSignificantControlService;

    @Mock
    private PersonWithSignificantControlRepository repository;

    @Mock
    private PersonWithSignificantControlMapper mapper;

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<PersonWithSignificantControlDao> pscDaoArgumentCaptor;

    @Captor
    private ArgumentCaptor<PersonWithSignificantControlDto> pscDtoCaptor;

    @Test
    void testGetPersonWithSignificantControlSuccess() throws ServiceException {
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personPersonWithSignificantControlDao().build();

        when(repository.findById(PSC_ID))
                .thenReturn(Optional.of(dao));

        when(mapper.daoToDto(dao)).thenReturn(new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personPersonWithSignificantControlDto().build());
        when(transactionService.isTransactionLinkedToResource(any(), anyString(), anyString()))
                .thenReturn(true);

        var dto = personWithSignificantControlService.getPersonWithSignificantControl(TRANSACTION, PSC_ID);
        assertEquals("John", dto.getData().getForename());
        assertEquals("Smith", dto.getData().getSurname());
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
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personPersonWithSignificantControlDao().build();

        when(repository.findById(PSC_ID))
                .thenReturn(Optional.of(dao));

        when(transactionService.isTransactionLinkedToResource(eq(TRANSACTION), any(String.class), any(String.class)))
                .thenReturn(false);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> personWithSignificantControlService.getPersonWithSignificantControl(TRANSACTION, PSC_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testCreatePersonWithSignificantControlReturnsSuccess() throws ServiceException {
        var submissionUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);
        PersonWithSignificantControlDto dto =  new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build();
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().build();

        when(mapper.dtoToDao(dto)).thenReturn(dao);
        when(repository.insert(dao)).thenReturn(dao);

        String submissionId = personWithSignificantControlService.createPersonWithSignificantControl(TRANSACTION, dto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(dto);
        verify(repository, times(1)).insert(dao);
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());
        verify(transactionService, times(1)).updateTransactionWithLinksForResource(REQUEST_ID, TRANSACTION, submissionUri, dao.getData().getKind(), null);

        PersonWithSignificantControlDao sentSubmission = pscDaoArgumentCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL, sentSubmission.getData().getKind());
        assertEquals(PSC_ID, submissionId);

        // Assert self link
        String expectedUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void testCreatePscWithNullKindDefaultsToPersonWithSignificantControl() throws ServiceException {
        PersonWithSignificantControlDto dto =  new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build();
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().withKind(null).build();

        when(mapper.dtoToDao(dto)).thenReturn(dao);
        when(repository.insert(dao)).thenReturn(dao);

        personWithSignificantControlService.createPersonWithSignificantControl(TRANSACTION, dto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(dto);
        verify(repository, times(1)).insert(dao);
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao sentSubmission = pscDaoArgumentCaptor.getValue();
        assertEquals(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL, sentSubmission.getData().getKind());
    }

    @Test
    void testUpdatePersonWithSignificantControlPersistsUpdatedFieldsSuccessfully() throws ServiceException {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        PersonWithSignificantControlDao existingDao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().build();
        PersonWithSignificantControlDto existingDto = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build();
        PersonWithSignificantControlDataDto changesDataDto = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDtoForPatch().build().getData();
        PersonWithSignificantControlDao afterPatchDao = new PersonWithSignificantControlDao();

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(true);
        when(mapper.daoToDto(existingDao)).thenReturn(existingDto);
        when(mapper.dtoToDao(existingDto)).thenReturn(afterPatchDao);

        assertNull(afterPatchDao.getId());
        assertNull(afterPatchDao.getCreatedAt());
        assertNull(afterPatchDao.getCreatedBy());
        assertNull(afterPatchDao.getLinks());
        assertNull(afterPatchDao.getTransactionId());

        personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, changesDataDto, REQUEST_ID, USER_ID);

        verify(repository, times(1)).findById(PSC_ID);
        verify(mapper, times(1)).daoToDto(existingDao);
        verify(mapper, times(1)).update(changesDataDto, existingDto.getData());
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedDao = pscDaoArgumentCaptor.getValue();
        assertEquals(existingDao.getId(), savedDao.getId());
        assertEquals(existingDao.getCreatedAt(), savedDao.getCreatedAt());
        assertEquals(existingDao.getCreatedBy(), savedDao.getCreatedBy());
        assertEquals(existingDao.getLinks(), savedDao.getLinks());
        assertEquals(existingDao.getTransactionId(), savedDao.getTransactionId());
        assertEquals(USER_ID, savedDao.getUpdatedBy());
    }

    @Test
    void testUpdatePersonWithSignificantControlRemovesSecondNationalityWhenPatchedToNull() throws ResourceNotFoundException {
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        PersonWithSignificantControlDao existingDao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().build();
        PersonWithSignificantControlDto existingDto = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build();
        PersonWithSignificantControlDataDto changesDataDto = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDtoForPatch().build().getData();
        PersonWithSignificantControlDao afterPatchDao = new PersonWithSignificantControlDao();

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(true);
        when(mapper.daoToDto(existingDao)).thenReturn(existingDto);
        when(mapper.dtoToDao(existingDto)).thenReturn(afterPatchDao);

        assertNotNull(existingDto.getData().getNationality2());

        personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, changesDataDto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).update(changesDataDto, existingDto.getData());
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
        PersonWithSignificantControlDao existingDao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().build();
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> personWithSignificantControlService.updatePersonWithSignificantControl(TRANSACTION, PSC_ID, new PersonWithSignificantControlDataDto(), REQUEST_ID, USER_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testDeletePersonWithSignificantControlSuccess() throws ServiceException {
        PersonWithSignificantControlDao existingDao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().build();
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
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
        PersonWithSignificantControlDao existingDao = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personWithSignificantControlDao().build();
        var pscUri = String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> personWithSignificantControlService.deletePersonWithSignificantControl(TRANSACTION, PSC_ID, REQUEST_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }
}
