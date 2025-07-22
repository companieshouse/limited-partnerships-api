package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;

@Testcontainers
@SpringBootTest
class LimitedPartnershipRepositoryTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private LimitedPartnershipRepository repository;

    @Test
     void testAuditFieldsArePopulated() {
        LocalDateTime startOfTest = LocalDateTime.now();
        LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipDao();
        repository.insert(limitedPartnershipDao);

        assertThat(limitedPartnershipDao.getCreatedAt()).isBetween(startOfTest, LocalDateTime.now());
        assertThat(limitedPartnershipDao.getUpdatedAt()).isBetween(startOfTest, LocalDateTime.now());
    }
}
