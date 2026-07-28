package uk.gov.companieshouse.limitedpartnershipsapi.incorporation;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dao.IncorporationDao;

@Repository
public interface IncorporationRepository extends MongoRepository<IncorporationDao, String> {
}
