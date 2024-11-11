package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.time.LocalDateTime;

@Service
public class LimitedPartnershipService {

    private final LimitedPartnershipMapper mapper;
    private final LimitedPartnershipSubmissionsRepository repository;

    @Autowired
    public LimitedPartnershipService(LimitedPartnershipMapper mapper, LimitedPartnershipSubmissionsRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    public String createLimitedPartnership(LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto, String requestId) {
        ApiLogger.debug("Called createLimitedPartnership(...)");

        LimitedPartnershipSubmissionDao dao = mapper.dtoToDao(limitedPartnershipSubmissionDto);
        dao.setCreatedAt(LocalDateTime.now());
        LimitedPartnershipSubmissionDao insertedSubmission = repository.insert(dao);
        ApiLogger.infoContext(requestId, String.format("Limited Partnership Submission created with limited-partnership submission id: %s", insertedSubmission.getId()));

        return insertedSubmission.getId();
    }
}
