package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.limitedpartnershipsapi.config.MongoTestConfig.TEST_DATE_TIME;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.limitedpartnershipsapi.config.MongoTestConfig;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;

@Disabled("Disabled until we have a test container for MongoDB in pipeline")
@DataMongoTest
@ActiveProfiles("test")
@Import(MongoTestConfig.class)
public class LimitedPartnerRepositoryTest {

    @Autowired
    private LimitedPartnerRepository repository;

    @Test
    public void testAuditFieldsArePopulated(){
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerDao();
        repository.insert(limitedPartnerDao);

        assertThat(limitedPartnerDao.getCreatedAt()).isEqualTo(TEST_DATE_TIME);
        assertThat(limitedPartnerDao.getUpdatedAt()).isEqualTo(TEST_DATE_TIME);
    }
}
