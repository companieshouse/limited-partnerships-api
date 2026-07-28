package uk.gov.companieshouse.limitedpartnershipsapi.incorporation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dao.IncorporationDao;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class IncorporationRepositoryTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private IncorporationRepository repository;

    @Test
    void testAuditFieldsArePopulated() {
        LocalDateTime startOfTest = LocalDateTime.now();
        IncorporationDao incorporationDao = new IncorporationDao();
        repository.insert(incorporationDao);

        assertThat(incorporationDao.getCreatedAt()).isBetween(startOfTest, LocalDateTime.now());
        assertThat(incorporationDao.getUpdatedAt()).isBetween(startOfTest, LocalDateTime.now());
    }
}
