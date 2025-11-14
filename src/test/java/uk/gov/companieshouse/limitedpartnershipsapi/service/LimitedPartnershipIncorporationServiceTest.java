package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipIncorporationMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.IncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode.TRANSITION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_COSTS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_VALIDATION_STATUS;
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
    private LimitedPartnershipIncorporationMapper mapper;

    @Mock
    private LimitedPartnershipService limitedPartnershipService;

    @Mock
    private LimitedPartnerService limitedPartnerService;

    @Mock
    private GeneralPartnerService generalPartnerService;

    @Captor
    private ArgumentCaptor<Transaction> transactionSubmissionCaptor;

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    Transaction transaction = new TransactionBuilder().build();

    @Test
    void testCreateIncorporationIsSuccessful() throws ServiceException {
        // given
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
    void shouldAddCorrectLinksToTransactionResourceForRegistration() throws ServiceException {
        // given + when
        createIncorporation(REGISTRATION);

        // then
        verify(transactionService).updateTransaction(transactionSubmissionCaptor.capture(), eq(REQUEST_ID));

        Map<String, Resource> transactionResources = transactionSubmissionCaptor.getValue().getResources();
        assertEquals(1, transactionResources.size());
        assertThat(transactionResources.values())
                .allSatisfy(resource -> assertThat(resource.getLinks())
                        .hasSize(3)
                        .isNotNull()
                        .containsKeys(LINK_RESOURCE, LINK_VALIDATION_STATUS, LINK_COSTS));
    }

    @Test
    void shouldAddCorrectLinksToTransactionResourceForTransition() throws ServiceException {
        // given + when
        createIncorporation(TRANSITION);

        // then
        verify(transactionService).updateTransaction(transactionSubmissionCaptor.capture(), eq(REQUEST_ID));

        Map<String, Resource> transactionResources = transactionSubmissionCaptor.getValue().getResources();
        assertEquals(1, transactionResources.size());
        assertThat(transactionResources.values())
                .allSatisfy(resource -> assertThat(resource.getLinks())
                        .hasSize(2)
                        .isNotNull()
                        .containsKeys(LINK_RESOURCE, LINK_VALIDATION_STATUS));
    }

    @Test
    void testGetIncorporationTypeWithoutSubResourcesIsSuccessful() throws ServiceException {
        // given
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
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
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();
        List<LimitedPartnerDto> limitedPartnerList = List.of(new LimitedPartnerDto());
        List<GeneralPartnerDto> generalPartnerList = List.of(new GeneralPartnerDto());
        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipIncorporationDao));
        when(mapper.daoToDto(limitedPartnershipIncorporationDao)).thenReturn(createLimitedPartnershipIncorporationDto());
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(
                limitedPartnershipDto);
        when(limitedPartnerService.getLimitedPartnerList(transaction)).thenReturn(limitedPartnerList);
        when(generalPartnerService.getGeneralPartnerList(transaction)).thenReturn(generalPartnerList);

        // when
        var limitedPartnershipIncorporationDto = incorporationService.getIncorporation(transaction, SUBMISSION_ID, true);

        // then
        assertNotNull(limitedPartnershipIncorporationDto);
        assertEquals(REGISTRATION.getDescription(), limitedPartnershipIncorporationDto.getKind());
        assertNotNull(limitedPartnershipIncorporationDto.getSubResources());
        assertNotNull(limitedPartnershipIncorporationDto.getSubResources().getPartnership());
        assertEquals(limitedPartnershipDto.getData(), limitedPartnershipIncorporationDto.getSubResources().getPartnership().getData());
        assertThat(limitedPartnershipIncorporationDto.getSubResources().getLimitedPartners(), containsInAnyOrder(limitedPartnerList.toArray()));
        assertThat(limitedPartnershipIncorporationDto.getSubResources().getGeneralPartners(), containsInAnyOrder(generalPartnerList.toArray()));
    }

    @Test
    void testGetIncorporationTypeReturnsNotFoundExceptionWhenNoLinkBetweenTransactionAndIncorporation() {
        // given
        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> incorporationService.getIncorporation(transaction, SUBMISSION_ID, true));
    }

    @Test
    void testGetIncorporationTypeReturnsNotFoundExceptionWhenIncorporationNotFoundInMongo() {
        final String INVALID_SUBMISSION_ID = "wrong-id";

        // given
        when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(INVALID_SUBMISSION_ID)).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> incorporationService.getIncorporation(transaction, INVALID_SUBMISSION_ID, true));
    }

    private LimitedPartnershipIncorporationDao createLimitedPartnershipIncorporationDao() {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.setId(SUBMISSION_ID);
        dao.getData().setKind(REGISTRATION.getDescription());

        return dao;
    }

    private LimitedPartnershipIncorporationDto createLimitedPartnershipIncorporationDto() {
        LimitedPartnershipIncorporationDto dto = new LimitedPartnershipIncorporationDto();
        dto.setKind(REGISTRATION.getDescription());
        return dto;
    }

    private void createIncorporation(FilingMode filingMode) throws ServiceException {
        // given
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(repository.insert(any(LimitedPartnershipIncorporationDao.class))).thenReturn(limitedPartnershipIncorporationDao);
        when(transactionService.isForRegistration(transaction)).thenReturn(REGISTRATION.equals(filingMode));

        IncorporationDto incorporationDto = new IncorporationDto();
        IncorporationDataDto dataDto = new IncorporationDataDto();
        dataDto.setKind(filingMode);
        incorporationDto.setData(dataDto);

        // when
        incorporationService.createIncorporation(transaction, incorporationDto, REQUEST_ID, USER_ID);
    }
}
