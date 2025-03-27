package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;

import java.util.List;

@Repository
public interface LimitedPartnershipRepository extends MongoRepository<LimitedPartnershipDao, String> {

    List<LimitedPartnershipDao> findByTransactionId(String transactionId);
}
