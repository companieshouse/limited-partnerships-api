package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;

public interface PersonWithSignificantControlRepository extends MongoRepository<PersonWithSignificantControlDao, String> {
    List<PersonWithSignificantControlDao> findAllByTransactionIdOrderByUpdatedAtDesc(String transactionId);
}
