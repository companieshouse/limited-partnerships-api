package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;

import java.util.List;

@Repository
public interface PartnershipRepository extends MongoRepository<PartnershipDao, String> {

    List<PartnershipDao> findByTransactionId(String transactionId);
}
