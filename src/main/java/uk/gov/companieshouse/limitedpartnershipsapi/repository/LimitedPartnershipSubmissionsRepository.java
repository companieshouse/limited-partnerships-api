package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipSubmissionDao;

import java.util.List;

@Repository
public interface LimitedPartnershipSubmissionsRepository extends MongoRepository<LimitedPartnershipSubmissionDao, String> {

    List<LimitedPartnershipSubmissionDao> findByTransactionId(String transactionId);
}
