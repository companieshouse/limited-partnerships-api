package uk.gov.companieshouse.limitedpartnershipsapi.generalpartner;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dao.GeneralPartnerDao;

import java.util.List;

public interface GeneralPartnerRepository extends MongoRepository<GeneralPartnerDao, String> {
    List<GeneralPartnerDao> findAllByTransactionIdOrderByUpdatedAtDesc(String transactionId);
}
