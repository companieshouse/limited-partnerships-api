package uk.gov.companieshouse.limitedpartnershipsapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;

@ExtendWith(MockitoExtension.class)
class LimitedPartnershipIncorporationServiceTest {

    @InjectMocks
    LimitedPartnershipIncorporationService incorporationService;

    @Mock
    LimitedPartnershipIncorporationRepository repository;

    @Captor
    private ArgumentCaptor<LimitedPartnershipIncorporationDao> incorporationCaptor;

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "12321123";

    @Test
    void testCreateIncorporationTypeIsSuccessful() {
        // given
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(repository.insert(any(LimitedPartnershipIncorporationDao.class))).thenReturn(limitedPartnershipIncorporationDao);

        // when
        var submissionId = incorporationService.createIncorporationType(USER_ID, TRANSACTION_ID);

        // then
        verify(repository, times(1)).insert(incorporationCaptor.capture());
        assertEquals(SUBMISSION_ID, submissionId);

        // assert dao incorporation self link is correct
        String submissionUri = String.format(URL_GET_INCORPORATION, TRANSACTION_ID, submissionId);
        LimitedPartnershipIncorporationDao sentSubmission = incorporationCaptor.getValue();
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

    private LimitedPartnershipIncorporationDao createLimitedPartnershipIncorporationDao() {
        LimitedPartnershipIncorporationDao dao = new LimitedPartnershipIncorporationDao();
        dao.setId(SUBMISSION_ID);
        return dao;
    }
}
