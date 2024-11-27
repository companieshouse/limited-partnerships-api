package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LimitedPartnershipService {

    private final LimitedPartnershipMapper mapper;
    private final LimitedPartnershipSubmissionsRepository repository;

    @Autowired
    public LimitedPartnershipService(LimitedPartnershipMapper mapper, LimitedPartnershipSubmissionsRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    public String createLimitedPartnership(Transaction transaction, LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto, String requestId, String userId) {
        ApiLogger.debug("Called createLimitedPartnership(...)");

        LimitedPartnershipSubmissionDao dao = mapper.dtoToDao(limitedPartnershipSubmissionDto);
        dao.setCreatedAt(LocalDateTime.now());
        dao.setUserId(userId);

        LimitedPartnershipSubmissionDao insertedSubmission = repository.insert(dao);

        // Create the self-link
        String selfLink = String.format("/transactions/{transaction_id}/limited_partnership/partnership/{filing_resource_id}", transaction.getId(), dao.getId());
        Map<String, String> links = new HashMap<>();
        links.put("self", selfLink);
        dao.setLinks(links);

        
        ApiLogger.infoContext(requestId, String.format("Limited Partnership Submission created with limited-partnership submission id: %s", insertedSubmission.getId()));

        return insertedSubmission.getId();
    }
}
