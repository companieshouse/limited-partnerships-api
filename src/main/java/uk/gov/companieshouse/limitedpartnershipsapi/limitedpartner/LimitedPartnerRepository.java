package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dao.LimitedPartnerDao;

import java.util.List;

public interface LimitedPartnerRepository extends MongoRepository<LimitedPartnerDao, String> {
    List<LimitedPartnerDao> findAllByTransactionIdOrderByUpdatedAtDesc(String transactionId);
}
