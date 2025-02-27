package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;

import java.util.List;

public interface LimitedPartnerRepository extends MongoRepository<LimitedPartnerDao, String> {
    List<LimitedPartnerDao> findByTransactionId(String transactionId);
}
