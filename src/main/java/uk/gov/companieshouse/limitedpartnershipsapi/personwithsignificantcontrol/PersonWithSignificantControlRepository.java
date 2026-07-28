package uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;

import java.util.List;

public interface PersonWithSignificantControlRepository extends MongoRepository<PersonWithSignificantControlDao, String> {
    List<PersonWithSignificantControlDao> findAllByTransactionIdOrderByUpdatedAtDesc(String transactionId);
}
