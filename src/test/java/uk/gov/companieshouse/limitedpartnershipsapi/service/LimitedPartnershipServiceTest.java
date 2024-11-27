package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class LimitedPartnershipServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String TRANSACTION_ID = "txn-456";
    private static final String SELF_LINK = String.format("/transactions/txn-456/limited_partnership/partnership/abc-123", TRANSACTION_ID, SUBMISSION_ID);


    @InjectMocks
    private LimitedPartnershipService service;

    @Mock
    private LimitedPartnershipMapper mapper;

    @Mock
    private LimitedPartnershipSubmissionsRepository repository;

    @Test
    public void givenDto_whenCreateLP_thenLPCreatedWithSubmissionId() {
        // given
        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();

        when(mapper.dtoToDao(limitedPartnershipSubmissionDto)).thenReturn(limitedPartnershipSubmissionDao);
        when(repository.insert(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDao);

        // when
        String submissionId = service.createLimitedPartnership(createTransaction(),limitedPartnershipSubmissionDto, REQUEST_ID, USER_ID);

        //then
        verify(mapper, times(1)).dtoToDao(limitedPartnershipSubmissionDto);
        verify(repository, times(1)).insert(limitedPartnershipSubmissionDao);
        assertEquals(SUBMISSION_ID, submissionId);
        assertEquals(SELF_LINK, limitedPartnershipSubmissionDao.getLinks().get("self"));
    }

    private LimitedPartnershipSubmissionDao createDao() {
        LimitedPartnershipSubmissionDao dao = new LimitedPartnershipSubmissionDao();
        dao.setId(SUBMISSION_ID);
        dao.setCreatedAt(LocalDateTime.now());
        dao.setUserId(USER_ID);
        Map<String, String> links = new HashMap<>();
        links.put("self", SELF_LINK);
        dao.setLinks(links);
        return dao;
    }

    private LimitedPartnershipSubmissionDto createDto() {
        return new LimitedPartnershipSubmissionDto();
    }

    private Transaction createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }
}
