package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;

import java.util.List;

public interface GeneralPartnerRepository extends MongoRepository<GeneralPartnerDao, String> {
    List<GeneralPartnerDao> findByTransactionId(String transactionId);
}
