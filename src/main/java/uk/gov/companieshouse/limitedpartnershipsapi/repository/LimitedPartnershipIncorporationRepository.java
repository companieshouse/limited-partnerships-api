package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;

@Repository
public interface LimitedPartnershipIncorporationRepository extends MongoRepository<LimitedPartnershipIncorporationDao, String> {
}
