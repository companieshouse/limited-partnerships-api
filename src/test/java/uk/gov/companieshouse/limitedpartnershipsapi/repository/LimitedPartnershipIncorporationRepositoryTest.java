package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.limitedpartnershipsapi.config.MongoTestConfig.TEST_DATE_TIME;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.limitedpartnershipsapi.config.MongoTestConfig;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;

@DataMongoTest
@ActiveProfiles("test")
@Import(MongoTestConfig.class)
public class LimitedPartnershipIncorporationRepositoryTest {
    @Autowired
    private LimitedPartnershipIncorporationRepository repository;

    @Test
    public void testAuditFieldsArePopulated(){
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = new LimitedPartnershipIncorporationDao();
        repository.insert(limitedPartnershipIncorporationDao);

        assertThat(limitedPartnershipIncorporationDao.getCreatedAt()).isEqualTo(TEST_DATE_TIME);
        assertThat(limitedPartnershipIncorporationDao.getUpdatedAt()).isEqualTo(TEST_DATE_TIME);
    }
}
