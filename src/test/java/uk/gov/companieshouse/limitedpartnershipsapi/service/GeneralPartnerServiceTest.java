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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

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
    private static final String SUBMISSION_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    Transaction transaction = new TransactionBuilder().forPartner(
                    FILING_KIND_GENERAL_PARTNER,
                    URL_GET_GENERAL_PARTNER,
                    GeneralPartnerBuilder.GENERAL_PARTNER_ID
            )
            .build();

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

    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

    @Test
    void testGetGeneralPartnerSuccess() throws ServiceException {
        GeneralPartnerDao dao = new GeneralPartnerBuilder().personDao();

        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.of(dao));

        when(mapper.daoToDto(dao)).thenReturn(new GeneralPartnerBuilder().personDto());
        when(transactionService.isTransactionLinkedToPartner(any(), anyString(), anyString()))
                .thenReturn(true);

        var dto = generalPartnerService.getGeneralPartner(transaction, SUBMISSION_ID);
        assertEquals("Jack", dto.getData().getForename());
        assertEquals("Jones", dto.getData().getSurname());
    }

    @Test
    void testGetGeneralPartnerNotFound() {
        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.empty());
        when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), any(String.class))).thenReturn(true);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> generalPartnerService.getGeneralPartner(transaction, SUBMISSION_ID));
        assertEquals("General partner submission with id " + SUBMISSION_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testGetGeneralPartnerLinkFails() {
        when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), any(String.class)))
                .thenReturn(false);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> generalPartnerService.getGeneralPartner(transaction, SUBMISSION_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches general partner id: %s", transaction.getId(), SUBMISSION_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testCreateLinksForGeneralPartnerReturnsSuccess() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

        GeneralPartnerDto dto = new GeneralPartnerBuilder().personDto();
        GeneralPartnerDao dao = new GeneralPartnerBuilder().personDao();

        when(mapper.dtoToDao(dto)).thenReturn(dao);
        when(repository.insert(dao)).thenReturn(dao);

        String submissionId = generalPartnerService.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(dto);
        verify(repository, times(1)).insert(dao);
        verify(repository, times(1)).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_GENERAL_PARTNER, sentSubmission.getData().getKind());
        assertEquals(SUBMISSION_ID, submissionId);

        // Assert self link
        String expectedUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), SUBMISSION_ID);
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
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(new GeneralPartnerBuilder().personDao()));
        when(mapper.daoToDto(any(GeneralPartnerDao.class))).thenReturn(new GeneralPartnerBuilder().personDto());
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        List<GeneralPartnerDataDto> generalPartnerDataDtoList = generalPartnerService.getGeneralPartnerDataList(transaction);
        assertEquals(1, generalPartnerDataDtoList.size());
    }

    @Test
    void testGetGeneralPartnerList() throws ServiceException {
        GeneralPartnerDao generalPartnerDao1 = new GeneralPartnerBuilder().personDao();
        generalPartnerDao1.setTransactionId(TransactionBuilder.TRANSACTION_ID);
        GeneralPartnerDao generalPartnerDao2 = new GeneralPartnerBuilder().personDao();
        generalPartnerDao2.setTransactionId(TransactionBuilder.TRANSACTION_ID);
        List<GeneralPartnerDao> generalPartnerDaoList = List.of(generalPartnerDao1, generalPartnerDao2);

        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TransactionBuilder.TRANSACTION_ID)).thenReturn(generalPartnerDaoList);

        GeneralPartnerDto generalPartnerDto1 = new GeneralPartnerBuilder().personDto();
        GeneralPartnerDto generalPartnerDto2 = new GeneralPartnerBuilder().personDto();

        when(mapper.daoToDto(generalPartnerDao1)).thenReturn(generalPartnerDto1);
        when(mapper.daoToDto(generalPartnerDao2)).thenReturn(generalPartnerDto2);

        List<GeneralPartnerDto> generalPartnerDtoList = generalPartnerService.getGeneralPartnerList(transaction);

        assertThat(generalPartnerDtoList).containsExactly(generalPartnerDto1, generalPartnerDto2);
    }

    @Test
    void testGetGeneralPartnerList_Empty() throws ServiceException {
        when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(transaction.getId())).thenReturn(new ArrayList<>());

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
}
