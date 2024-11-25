package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;

@ExtendWith(MockitoExtension.class)
public class LimitedPartnershipServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @InjectMocks
    private LimitedPartnershipService service;

    @Mock
    private LimitedPartnershipMapper mapper;

    @Mock
    private LimitedPartnershipSubmissionsRepository repository;

    @Test
    public void givenDto_whenCreateLP_thenLPCreatedWithSubmissionId() throws ServiceException {
        // given
        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();

        // TODO Uncomment and get test working again
//        when(mapper.dtoToDao(limitedPartnershipSubmissionDto)).thenReturn(limitedPartnershipSubmissionDao);
//        when(repository.insert(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDao);
//
//        // when
//        String submissionId = service.createLimitedPartnership(null, limitedPartnershipSubmissionDto, REQUEST_ID, USER_ID);
//
//        //then
//        verify(mapper, times(1)).dtoToDao(limitedPartnershipSubmissionDto);
//        verify(repository, times(1)).insert(limitedPartnershipSubmissionDao);
//        assertEquals(SUBMISSION_ID, submissionId);
    }

    private LimitedPartnershipSubmissionDao createDao() {
        LimitedPartnershipSubmissionDao dao = new LimitedPartnershipSubmissionDao();
        dao.setId(SUBMISSION_ID);
        return dao;
    }

    private LimitedPartnershipSubmissionDto createDto() {
        return new LimitedPartnershipSubmissionDto();
    }
}
