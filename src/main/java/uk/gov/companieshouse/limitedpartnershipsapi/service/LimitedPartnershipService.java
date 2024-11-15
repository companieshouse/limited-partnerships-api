package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

@Service
public class LimitedPartnershipService {

    private static final String PARTNERSHIP_URI_PATTERN = "/transactions/%s/limited_partnership/partnership/%s";
    private final LimitedPartnershipMapper mapper;
    private final LimitedPartnershipSubmissionsRepository repository;

    @Autowired
    public LimitedPartnershipService(LimitedPartnershipMapper mapper, LimitedPartnershipSubmissionsRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    public String createLimitedPartnership(LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto, String requestId, String userId) {
        ApiLogger.debug("Called createLimitedPartnership(...)");

        LimitedPartnershipSubmissionDao dao = mapper.dtoToDao(limitedPartnershipSubmissionDto);
        dao.setCreatedAt(LocalDateTime.now());
        dao.setUserId(userId);

        LimitedPartnershipSubmissionDao insertedSubmission = repository.insert(dao);
        String submissionId = insertedSubmission.getId();

        //TODO get transaction from controller
        String transactionId = "213123";
        updateSubmissionWithMetaData(transactionId, submissionId, dao);

        ApiLogger.infoContext(requestId, String.format("Limited Partnership Submission created with limited-partnership submission id: %s", submissionId));

        return submissionId;
    }

    private void updateSubmissionWithMetaData(String transactionId, String submissionId, LimitedPartnershipSubmissionDao dao) {
        final String submissionUri = getSubmissionUri(transactionId, submissionId);
        dao.setLinks(Collections.singletonMap("self", submissionUri));
        repository.save(dao);
    }

    private String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(PARTNERSHIP_URI_PATTERN, transactionId, submissionId);
    }
}
