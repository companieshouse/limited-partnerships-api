package uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;

import java.util.List;

@Repository
public interface PersonWithSignificantControlRepository extends MongoRepository<PersonWithSignificantControlDao, String> {
    List<PersonWithSignificantControlDao> findAllByTransactionIdOrderByUpdatedAtDesc(String transactionId);

    long countByTransactionId(String transactionId);
}
