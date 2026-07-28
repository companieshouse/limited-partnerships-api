package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class PartnershipRepositoryTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PartnershipRepository repository;

    @Test
     void testAuditFieldsArePopulated() {
        LocalDateTime startOfTest = LocalDateTime.now();
      PartnershipDao partnershipDao = new PartnershipDao();
      repository.insert(partnershipDao);

      assertThat(partnershipDao.getCreatedAt()).isBetween(startOfTest, LocalDateTime.now());
      assertThat(partnershipDao.getUpdatedAt()).isBetween(startOfTest, LocalDateTime.now());
    }
}
