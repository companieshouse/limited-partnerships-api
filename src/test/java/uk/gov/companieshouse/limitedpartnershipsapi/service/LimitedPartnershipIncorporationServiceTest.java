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
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService.LIMITED_PARTNERSHIP_REGISTRATION_KIND;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

@ExtendWith(MockitoExtension.class)
class LimitedPartnershipIncorporationServiceTest {

    @InjectMocks
    LimitedPartnershipIncorporationService incorporationService;

    @Mock
    LimitedPartnershipIncorporationRepository repository;

    @Captor
    private ArgumentCaptor<LimitedPartnershipIncorporationDao> incorporationCaptor;

    @Mock
    private TransactionUtils transactionUtils;

    @Mock
    private LimitedPartnershipIncorporationMapper mapper;

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "12321123";

    @Test
    void testCreateIncorporationIsSuccessful() {
        // given
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(repository.insert(any(LimitedPartnershipIncorporationDao.class))).thenReturn(limitedPartnershipIncorporationDao);

        // when
        var submissionId = incorporationService.createIncorporation(USER_ID, TRANSACTION_ID);

        // then
        verify(repository, times(1)).insert(incorporationCaptor.capture());
        assertEquals(SUBMISSION_ID, submissionId);

        // assert dao incorporation self link is correct
        String submissionUri = String.format(URL_GET_INCORPORATION, TRANSACTION_ID, submissionId);
        LimitedPartnershipIncorporationDao sentSubmission = incorporationCaptor.getValue();
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

//    @Test
//    void testGetIncorporationTypeIsSuccessful() throws ResourceNotFoundException {
//        // given
//        Transaction transaction = buildTransaction();
//        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
//        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
//        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipIncorporationDao));
//        when(mapper.daoToDto(limitedPartnershipIncorporationDao)).thenReturn(createLimitedPartnershipIncorporationDto());
//
//        // when
//        var limitedPartnershipIncorporationDto = incorporationService.getIncorporation(transaction, SUBMISSION_ID, true);
//
//        // then
//        assertNotNull(limitedPartnershipIncorporationDto);
//        assertEquals(LIMITED_PARTNERSHIP_REGISTRATION_KIND, limitedPartnershipIncorporationDto.getKind());
//    }

    @Test
    void testGetIncorporationTypeReturnsNotFoundExceptionWhenNoLinkBetweenTransactionAndIncorporation() throws ResourceNotFoundException {
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
        LimitedPartnershipIncorporationDao dao = new LimitedPartnershipIncorporationDao();
        dao.setId(SUBMISSION_ID);
        IncorporationDataDao dataDao = new IncorporationDataDao();
        dataDao.setKind(LIMITED_PARTNERSHIP_REGISTRATION_KIND);
        dao.setData(dataDao);
        return dao;
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private LimitedPartnershipIncorporationDto createLimitedPartnershipIncorporationDto() {
        LimitedPartnershipIncorporationDto dto = new LimitedPartnershipIncorporationDto();
        dto.setKind(LIMITED_PARTNERSHIP_REGISTRATION_KIND);
        return dto;
    }
}
