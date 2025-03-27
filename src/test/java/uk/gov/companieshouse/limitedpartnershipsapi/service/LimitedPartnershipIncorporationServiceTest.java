package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.IncorporationDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.IncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.IncorporationKind.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

@ExtendWith(MockitoExtension.class)
class LimitedPartnershipIncorporationServiceTest {

    @InjectMocks
    private LimitedPartnershipIncorporationService incorporationService;

    @Mock
    private LimitedPartnershipIncorporationRepository repository;

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnershipIncorporationDao> incorporationCaptor;

    @Mock
    private TransactionUtils transactionUtils;

    @Mock
    private LimitedPartnershipIncorporationMapper mapper;

    @Mock
    private LimitedPartnershipService limitedPartnershipService;

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "12321123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Test
    void testCreateIncorporationIsSuccessful() throws ServiceException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(repository.insert(any(LimitedPartnershipIncorporationDao.class))).thenReturn(limitedPartnershipIncorporationDao);

        IncorporationDto incorporationDto = new IncorporationDto();
        IncorporationDataDto dataDto = new IncorporationDataDto();
        dataDto.setKind(REGISTRATION);
        incorporationDto.setData(dataDto);

        // when
        var submissionId = incorporationService.createIncorporation(transaction, incorporationDto, REQUEST_ID, USER_ID);

        // then
        verify(repository, times(1)).insert(incorporationCaptor.capture());
        assertEquals(SUBMISSION_ID, submissionId);

        LimitedPartnershipIncorporationDao sentSubmission = incorporationCaptor.getValue();
        IncorporationDataDao dataDao = sentSubmission.getData();
        assertEquals(dataDto.getKind(), dataDao.getKind());
        assertNotNull(dataDao.getEtag());
        // assert dao incorporation self link is correct
        String submissionUri = String.format(URL_GET_INCORPORATION, TRANSACTION_ID, submissionId);
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);

        assertEquals(dataDto.getKind(), transaction.getFilingMode());
    }

    @Test
    void testGetIncorporationTypeWithoutSubResourcesIsSuccessful() throws ServiceException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipIncorporationDao));
        when(mapper.daoToDto(limitedPartnershipIncorporationDao)).thenReturn(createLimitedPartnershipIncorporationDto());

        // when
        var limitedPartnershipIncorporationDto = incorporationService.getIncorporation(transaction, SUBMISSION_ID, false);

        // then
        assertNotNull(limitedPartnershipIncorporationDto);
        assertEquals(REGISTRATION.getDescription(), limitedPartnershipIncorporationDto.getKind());
        assertNull(limitedPartnershipIncorporationDto.getSubResources());
    }

    @Test
    void testGetIncorporationTypeWithSubResourcesIsSuccessful() throws ServiceException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        LimitedPartnershipDto limitedPartnershipDto = createLimitedPartnershipSubmissionDto();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipIncorporationDao));
        when(mapper.daoToDto(limitedPartnershipIncorporationDao)).thenReturn(createLimitedPartnershipIncorporationDto());
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(
                limitedPartnershipDto);

        // when
        var limitedPartnershipIncorporationDto = incorporationService.getIncorporation(transaction, SUBMISSION_ID, true);

        // then
        assertNotNull(limitedPartnershipIncorporationDto);
        assertEquals(REGISTRATION.getDescription(), limitedPartnershipIncorporationDto.getKind());
        assertNotNull(limitedPartnershipIncorporationDto.getSubResources());
        assertNotNull(limitedPartnershipIncorporationDto.getSubResources().getPartnership());
        assertEquals(limitedPartnershipDto.getData(), limitedPartnershipIncorporationDto.getSubResources().getPartnership().getData());
    }

    @Test
    void testGetIncorporationTypeReturnsNotFoundExceptionWhenNoLinkBetweenTransactionAndIncorporation() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> incorporationService.getIncorporation(transaction, SUBMISSION_ID, true));
    }

    @Test
    void testGetIncorporationTypeReturnsNotFoundExceptionWhenIncorporationNotFoundInMongo() {
        final String INVALID_SUBMISSION_ID = "wrong-id";

        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(INVALID_SUBMISSION_ID)).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> incorporationService.getIncorporation(transaction, INVALID_SUBMISSION_ID, true));
    }

    private LimitedPartnershipIncorporationDao createLimitedPartnershipIncorporationDao() {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.setId(SUBMISSION_ID);
        dao.getData().setKind(REGISTRATION.getDescription());
        dao.setCreatedAt(LocalDateTime.now());

        return dao;
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private LimitedPartnershipIncorporationDto createLimitedPartnershipIncorporationDto() {
        LimitedPartnershipIncorporationDto dto = new LimitedPartnershipIncorporationDto();
        dto.setKind(REGISTRATION.getDescription());
        return dto;
    }

    private LimitedPartnershipDto createLimitedPartnershipSubmissionDto() {
        var submissionDto = new LimitedPartnershipDto();
        var dataDto = new DataDto();
        dataDto.setPartnershipName("Asset Strippers");
        dataDto.setNameEnding(PartnershipNameEnding.LP);
        submissionDto.setData(dataDto);

        return submissionDto;
    }
}
