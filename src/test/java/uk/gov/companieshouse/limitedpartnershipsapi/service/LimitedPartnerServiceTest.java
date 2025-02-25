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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
class LimitedPartnerServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @InjectMocks
    LimitedPartnerService service;

    @Mock
    LimitedPartnerRepository repository;

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnerDao> submissionCaptor;

    @Mock
    private LimitedPartnerMapper mapper;

    @Test
    void testCreateLimitedPartnerIsSuccessful() throws ServiceException {
        // given
        LimitedPartnerDto limitedPartnerDto = createDto();
        LimitedPartnerDao limitedPartnerDao = createDao();
        limitedPartnerDao.setId(SUBMISSION_ID);

        when(mapper.dtoToDao(limitedPartnerDto)).thenReturn(limitedPartnerDao);
        when(repository.insert(limitedPartnerDao)).thenReturn(limitedPartnerDao);

        Transaction testTransaction = buildTransaction();

        // when
        String submissionId = service.createLimitedPartner(testTransaction, limitedPartnerDto, REQUEST_ID, USER_ID);

        // then
        verify(mapper, times(1)).dtoToDao(limitedPartnerDto);
        verify(repository, times(1)).insert(limitedPartnerDao);
        verify(repository, times(1)).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_LIMITED_PARTNER, sentSubmission.getData().getKind());
        assertEquals(SUBMISSION_ID, submissionId);
        assertEquals(LimitedPartnerType.LEGAL_ENTITY, sentSubmission.getData().getPartnerType());

        // Assert self link
        String expectedUri = String.format(URL_GET_LIMITED_PARTNER, testTransaction.getId(), SUBMISSION_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void givenValidSubmissionUri_whenCreateLimitedPartnerTransactionResource_thenResourceCreated() {
        // given
        String submissionUri = "http://example.com/submission";

        // when
        Resource resource = createLimitedPartnerTransactionResource(submissionUri);

        // then
        assertNotNull(resource);
        assertEquals(FILING_KIND_LIMITED_PARTNER, resource.getKind());
        assertEquals(submissionUri, resource.getLinks().get("resource"));
    }

    @Test
    void givenNullSubmissionUri_whenCreateLimitedPartnerTransactionResource_thenResourceCreatedWithNullLink() {
        // given
        String submissionUri = null;

        // when
        Resource resource = createLimitedPartnerTransactionResource(submissionUri);

        // then
        assertNotNull(resource);
        assertEquals(FILING_KIND_LIMITED_PARTNER, resource.getKind());
        assertEquals(submissionUri, resource.getLinks().get("resource"));
    }

    @Test
    void testLimitedPartnerDtoInitialization() {
        LimitedPartnerDto limitedPartnerDto = new LimitedPartnerDto();
        LimitedPartnerDataDto limitedPartnerData = new LimitedPartnerDataDto();
        limitedPartnerDto.setData(limitedPartnerData);

        assertNotNull(limitedPartnerDto.getData());
    }

    private Resource createLimitedPartnerTransactionResource(String submissionUri) {
        var limitedPartnerResource = new Resource();
        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        limitedPartnerResource.setLinks(linksMap);
        limitedPartnerResource.setKind(FILING_KIND_LIMITED_PARTNER);
        return limitedPartnerResource;
    }

    private LimitedPartnerDto createDto() {
        LimitedPartnerDto dto = new LimitedPartnerDto();
        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setPartnerType(LimitedPartnerType.LEGAL_ENTITY);
        dto.setData(dataDto);
        return dto;
    }

    private LimitedPartnerDao createDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();
        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setPartnerType(LimitedPartnerType.LEGAL_ENTITY);
        dao.setData(dataDao);
        return dao;
    }

    public Transaction buildTransaction() {
        var transaction = new Transaction();
        transaction.setId("transaction-id");
        return transaction;
    }
}
