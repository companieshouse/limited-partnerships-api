package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;

public interface PscRepository extends MongoRepository<PscDao, String> {
}
