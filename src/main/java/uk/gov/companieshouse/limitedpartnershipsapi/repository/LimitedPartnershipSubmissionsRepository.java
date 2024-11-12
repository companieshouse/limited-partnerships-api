package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;

@Repository
public interface LimitedPartnershipSubmissionsRepository extends MongoRepository<LimitedPartnershipSubmissionDao, String> {
}
