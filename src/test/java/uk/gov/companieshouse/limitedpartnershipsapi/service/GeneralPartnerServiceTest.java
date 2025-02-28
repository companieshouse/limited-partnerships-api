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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    GeneralPartnerService generalPartnerService;
    @Mock
    GeneralPartnerRepository repository;
    @Mock
    GeneralPartnerMapper mapper;
    @Mock
    TransactionService transactionService;
    @Mock
    GeneralPartnerValidator generalPartnerValidator;
    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

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

//    @Test
//    void testExceptionIsThrownWhenFirstAndSecondNationalityIsTheSame() {
//        GeneralPartnerDto dto = createDto();
//        dto.getData().setNationality2(Nationality.BELGIAN);
//        Transaction testTransaction = buildTransaction();
//        assertThrows(MethodArgumentNotValidException.class, () -> { generalPartnerService.createGeneralPartner(testTransaction, dto, REQUEST_ID, USER_ID); });
//    }

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
        // given
        String submissionUri = null;

        // when
        Resource resource = createGeneralPartnerTransactionResource(submissionUri);

        // then
        assertNotNull(resource);
        assertEquals(FILING_KIND_GENERAL_PARTNER, resource.getKind());
        assertEquals(submissionUri, resource.getLinks().get("resource"));
    }

    @Test
    void testGeneralPartnerDtoInitialization() {
        GeneralPartnerDto generalPartnerDto = new GeneralPartnerDto();
        GeneralPartnerDataDto generalPartnerData = new GeneralPartnerDataDto();
        generalPartnerDto.setData(generalPartnerData);

        assertNotNull(generalPartnerDto.getData());
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
