package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PscMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PscRepository;

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
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PSC;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PSC;

@ExtendWith(MockitoExtension.class)
class PscServiceTest {

    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String PSC_ID = PscBuilder.ID;

    private static final Transaction TRANSACTION = new TransactionBuilder().withKindAndUri(
                    FILING_KIND_PSC,
                    URL_GET_PSC,
                    PSC_ID
            )
            .build();

    @InjectMocks
    private PscService pscService;

    @Mock
    private PscRepository repository;

    @Mock
    private PscMapper mapper;

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<PscDao> pscDaoArgumentCaptor;

    @Captor
    private ArgumentCaptor<PscDto> pscDtoCaptor;

    @Test
    void testGetPscSuccess() throws ServiceException {
        PscDao dao = PscBuilder.PscPersonDao();

        when(repository.findById(PSC_ID))
                .thenReturn(Optional.of(dao));

        when(mapper.daoToDto(dao)).thenReturn(PscBuilder.personPscDto());
        when(transactionService.isTransactionLinkedToResource(any(), anyString(), anyString()))
                .thenReturn(true);

        var dto = pscService.getPsc(TRANSACTION, PSC_ID);
        assertEquals("John", dto.getData().getForename());
        assertEquals("Smith", dto.getData().getSurname());
    }

    @Test
    void testGetPscNotFound() {
        when(repository.findById(PSC_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> pscService.getPsc(TRANSACTION, PSC_ID));
        assertEquals("Person with significant control resource with id " + PSC_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testTransactionLinkedToPscFails() {
        PscDao dao = PscBuilder.PscPersonDao();

        when(repository.findById(PSC_ID))
                .thenReturn(Optional.of(dao));

        when(transactionService.isTransactionLinkedToResource(eq(TRANSACTION), any(String.class), any(String.class)))
                .thenReturn(false);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> pscService.getPsc(TRANSACTION, PSC_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testCreatePscReturnsSuccess() throws ServiceException {
        var submissionUri = String.format(URL_GET_PSC, TRANSACTION.getId(), PSC_ID);
        PscDto dto =  PscBuilder.getPscDto();
        PscDao dao = PscBuilder.getPscDao();

        when(mapper.dtoToDao(dto)).thenReturn(dao);
        when(repository.insert(dao)).thenReturn(dao);

        String submissionId = pscService.createPsc(TRANSACTION, dto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(dto);
        verify(repository, times(1)).insert(dao);
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());
        verify(transactionService, times(1)).updateTransactionWithLinksForResource(REQUEST_ID, TRANSACTION, submissionUri, dao.getData().getKind(), null);

        PscDao sentSubmission = pscDaoArgumentCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_PSC, sentSubmission.getData().getKind());
        assertEquals(PSC_ID, submissionId);

        // Assert self link
        String expectedUri = String.format(URL_GET_PSC, TRANSACTION.getId(), PSC_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void testUpdatePscPersistsUpdatedFieldsSuccessfully() throws ServiceException {
        var pscUri = String.format(URL_GET_PSC, TRANSACTION.getId(), PSC_ID);

        PscDao existingDao = PscBuilder.getPscDao();
        PscDto existingDto = PscBuilder.getPscDto();
        PscDataDto changesDataDto = PscBuilder.getPscDataDtoForPatch();
        PscDao afterPatchDao = new PscDao();

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PSC)).thenReturn(true);
        when(mapper.daoToDto(existingDao)).thenReturn(existingDto);
        when(mapper.dtoToDao(existingDto)).thenReturn(afterPatchDao);

        assertNull(afterPatchDao.getId());
        assertNull(afterPatchDao.getCreatedAt());
        assertNull(afterPatchDao.getCreatedBy());
        assertNull(afterPatchDao.getLinks());
        assertNull(afterPatchDao.getTransactionId());

        pscService.updatePsc(TRANSACTION, PSC_ID, changesDataDto, REQUEST_ID, USER_ID);

        verify(repository, times(1)).findById(PSC_ID);
        verify(mapper, times(1)).daoToDto(existingDao);
        verify(mapper, times(1)).update(changesDataDto, existingDto.getData());
        verify(repository, times(1)).save(pscDaoArgumentCaptor.capture());

        PscDao savedDao = pscDaoArgumentCaptor.getValue();
        assertEquals(existingDao.getId(), savedDao.getId());
        assertEquals(existingDao.getCreatedAt(), savedDao.getCreatedAt());
        assertEquals(existingDao.getCreatedBy(), savedDao.getCreatedBy());
        assertEquals(existingDao.getLinks(), savedDao.getLinks());
        assertEquals(existingDao.getTransactionId(), savedDao.getTransactionId());
        assertEquals(USER_ID, savedDao.getUpdatedBy());
    }

    @Test
    void testUpdatePscRemovesSecondNationalityWhenPatchedToNull() throws ResourceNotFoundException {
        var pscUri = String.format(URL_GET_PSC, TRANSACTION.getId(), PSC_ID);

        PscDao existingDao = PscBuilder.getPscDao();
        PscDto existingDto = PscBuilder.getPscDto();
        PscDataDto changesDataDto = PscBuilder.getPscDataDtoForPatch();
        PscDao afterPatchDao = new PscDao();

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PSC)).thenReturn(true);
        when(mapper.daoToDto(existingDao)).thenReturn(existingDto);
        when(mapper.dtoToDao(existingDto)).thenReturn(afterPatchDao);

        assertNotNull(existingDto.getData().getNationality2());

        pscService.updatePsc(TRANSACTION, PSC_ID, changesDataDto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).update(changesDataDto, existingDto.getData());
        verify(mapper, times(1)).dtoToDao(pscDtoCaptor.capture());

        var capturedDto = pscDtoCaptor.getValue();
        assertNull(capturedDto.getData().getNationality2());
    }

    @Test
    void testUpdatePscNotFound() {
        when(repository.findById(PSC_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> pscService.updatePsc(TRANSACTION, PSC_ID, new PscDataDto(), REQUEST_ID, USER_ID));
        assertEquals("Person with significant control with id " + PSC_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testUpdatePscTransactionNotLinked() {
        PscDao existingDao = PscBuilder.getPscDao();
        var pscUri = String.format(URL_GET_PSC, TRANSACTION.getId(), PSC_ID);

        when(repository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(TRANSACTION, pscUri, FILING_KIND_PSC)).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(
                ResourceNotFoundException.class,
                () -> pscService.updatePsc(TRANSACTION, PSC_ID, new PscDataDto(), REQUEST_ID, USER_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", TRANSACTION.getId(), PSC_ID), resourceNotFoundException.getMessage());
    }
}
