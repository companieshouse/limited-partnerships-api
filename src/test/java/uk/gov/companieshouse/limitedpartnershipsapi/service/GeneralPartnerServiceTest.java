package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
class GeneralPartnerServiceTest {

    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String SUBMISSION_ID = "submission123";

    @InjectMocks
    private GeneralPartnerService generalPartnerService;

    @Mock
    private GeneralPartnerRepository repository;

    @Mock
    private GeneralPartnerMapper mapper;

    @Mock
    private TransactionService transactionService;

    @Mock
    private GeneralPartnerValidator generalPartnerValidator;

    @Mock
    private TransactionUtils transactionUtils;

    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

    @Test
    void testGetGeneralPartnerSuccess() throws ServiceException {
        GeneralPartnerDao dao = createDao();

        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.of(dao));

        when(mapper.daoToDto(dao)).thenReturn(createDto());
        when(transactionUtils.isTransactionLinkedToPartnerSubmission(any(), anyString(), anyString()))
                .thenReturn(true);

        var dto = generalPartnerService.getGeneralPartner(buildTransaction(), SUBMISSION_ID);
        assertEquals("John", dto.getData().getForename());
        assertEquals("Doe", dto.getData().getSurname());
    }

    @Test
    void testGetGeneralPartnerNotFound() {
        Transaction transaction = new Transaction();
        transaction.setId("tran1234");

        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.empty());
        when(transactionUtils.isTransactionLinkedToPartnerSubmission(eq(transaction), any(String.class), any(String.class))).thenReturn(true);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> generalPartnerService.getGeneralPartner(transaction, SUBMISSION_ID));
        assertEquals("General partner submission with id submission123 not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testGetGeneralPartnerLinkFails() {
        Transaction transaction = new Transaction();
        transaction.setId("tran1234");

        when(transactionUtils.isTransactionLinkedToPartnerSubmission(eq(transaction), any(String.class), any(String.class)))
                .thenReturn(false);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> generalPartnerService.getGeneralPartner(transaction, SUBMISSION_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches general partner id: submission123", transaction.getId()), resourceNotFoundException.getMessage());
    }

    @Test
    void testCreateLinksForGeneralPartnerReturnsSuccess() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        GeneralPartnerDto dto = createDto();
        GeneralPartnerDao dao = createDao();
        dao.setId(SUBMISSION_ID);

        when(mapper.dtoToDao(dto)).thenReturn(dao);
        when(repository.insert(dao)).thenReturn(dao);

        Transaction testTransaction = buildTransaction();

        String submissionId = generalPartnerService.createGeneralPartner(testTransaction, dto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(dto);
        verify(repository, times(1)).insert(dao);
        verify(repository, times(1)).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_GENERAL_PARTNER, sentSubmission.getData().getKind());
        assertEquals(SUBMISSION_ID, submissionId);

        // Assert self link
        String expectedUri = String.format(URL_GET_GENERAL_PARTNER, testTransaction.getId(), SUBMISSION_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void givenValidSubmissionUri_whenCreateGeneralPartnerTransactionResource_thenResourceCreated() {
        // given
        String submissionUri = "http://example.com/submission";

        // when
        Resource resource = createGeneralPartnerTransactionResource(submissionUri);

        // then
        assertNotNull(resource);
        assertEquals(FILING_KIND_GENERAL_PARTNER, resource.getKind());
        assertEquals(submissionUri, resource.getLinks().get("resource"));
    }

    @Test
    void givenNullSubmissionUri_whenCreateGeneralPartnerTransactionResource_thenResourceCreatedWithNullLink() {
        // given + when
        Resource resource = createGeneralPartnerTransactionResource(null);

        // then
        assertNotNull(resource);
        assertEquals(FILING_KIND_GENERAL_PARTNER, resource.getKind());
        assertNull(resource.getLinks().get("resource"));
    }

    @Test
    void testGeneralPartnerDtoInitialization() {
        GeneralPartnerDto generalPartnerDto = new GeneralPartnerDto();
        GeneralPartnerDataDto generalPartnerData = new GeneralPartnerDataDto();
        generalPartnerDto.setData(generalPartnerData);

        assertNotNull(generalPartnerDto.getData());
    }

    @Test
    void testGetGeneralPartnerDataList() {
        var transactionId = "trns123";
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(transactionId)).thenReturn(List.of(createDao()));
        when(mapper.daoToDto(any(GeneralPartnerDao.class))).thenReturn(createDto());
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        List<GeneralPartnerDataDto> generalPartnerDataDtoList = generalPartnerService.getGeneralPartnerDataList(transaction);
        assertEquals(1, generalPartnerDataDtoList.size());
    }

    @Test
    void testGetGeneralPartnerList() {
        var transactionId = "9324234-234324-324";
        GeneralPartnerDao generalPartnerDao1 = createDao();
        GeneralPartnerDao generalPartnerDao2 = createDao();
        List<GeneralPartnerDao> generalPartnerDaoList = List.of(generalPartnerDao1, generalPartnerDao2);
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(transactionId)).thenReturn(generalPartnerDaoList);
        GeneralPartnerDto generalPartnerDto1 = createDto();
        GeneralPartnerDto generalPartnerDto2 = createDto();
        when(mapper.daoToDto(generalPartnerDao1)).thenReturn(generalPartnerDto1);
        when(mapper.daoToDto(generalPartnerDao2)).thenReturn(generalPartnerDto2);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        List<GeneralPartnerDto> generalPartnerDtoList = generalPartnerService.getGeneralPartnerList(transaction);

        assertThat(generalPartnerDtoList).containsExactly(generalPartnerDto1, generalPartnerDto2);
    }

    @Test
    void testGetGeneralPartnerList_Empty() {
        var transactionId = "9324234-234324-324";
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(transactionId)).thenReturn(new ArrayList<>());

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        List<GeneralPartnerDto> generalPartnerDtoList = generalPartnerService.getGeneralPartnerList(transaction);

        assertEquals(0, generalPartnerDtoList.size());
    }

    private Resource createGeneralPartnerTransactionResource(String submissionUri) {
        var generalPartnerResource = new Resource();
        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        generalPartnerResource.setLinks(linksMap);
        generalPartnerResource.setKind(FILING_KIND_GENERAL_PARTNER);
        return generalPartnerResource;
    }

    private GeneralPartnerDto createDto() {
        GeneralPartnerDto dto = new GeneralPartnerDto();
        GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
        dataDto.setForename("John");
        dataDto.setSurname("Doe");
        dataDto.setNationality1(Nationality.BELGIAN);
        dto.setData(dataDto);
        return dto;
    }

    private GeneralPartnerDao createDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();
        dao.setUpdatedAt(LocalDateTime.now());
        dao.setCreatedAt(LocalDateTime.now());

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dao.setData(dataDao);
        return dao;
    }

    public Transaction buildTransaction() {
        var transaction = new Transaction();
        transaction.setId("transaction-id");
        return transaction;
    }
}
