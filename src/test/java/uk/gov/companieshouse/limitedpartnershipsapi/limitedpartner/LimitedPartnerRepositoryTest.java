package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dao.LimitedPartnerDao;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class LimitedPartnerRepositoryTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private LimitedPartnerRepository repository;

    @Test
    void testAuditFieldsArePopulated() {
        LocalDateTime startOfTest = LocalDateTime.now();
        LimitedPartnerDao limitedPartnerDao = new LimitedPartnerDao();
        repository.insert(limitedPartnerDao);

        assertThat(limitedPartnerDao.getCreatedAt()).isBetween(startOfTest, LocalDateTime.now());
        assertThat(limitedPartnerDao.getUpdatedAt()).isBetween(startOfTest, LocalDateTime.now());
    }
}
