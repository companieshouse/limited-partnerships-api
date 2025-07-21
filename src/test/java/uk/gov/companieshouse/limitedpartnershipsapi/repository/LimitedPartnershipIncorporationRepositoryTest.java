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
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;

@Disabled("Disabled until we have a test container for MongoDB in pipeline")
@DataMongoTest
@ActiveProfiles("test")
@Import(MongoTestConfig.class)
class LimitedPartnershipIncorporationRepositoryTest {
    @Autowired
    private LimitedPartnershipIncorporationRepository repository;

    @Test
    void testAuditFieldsArePopulated(){
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = new LimitedPartnershipIncorporationDao();
        repository.insert(limitedPartnershipIncorporationDao);

        assertThat(limitedPartnershipIncorporationDao.getCreatedAt()).isEqualTo(TEST_DATE_TIME);
        assertThat(limitedPartnershipIncorporationDao.getUpdatedAt()).isEqualTo(TEST_DATE_TIME);
    }
}
