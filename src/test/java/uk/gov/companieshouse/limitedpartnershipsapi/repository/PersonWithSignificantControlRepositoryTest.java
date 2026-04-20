package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
@SpringBootTest
class PersonWithSignificantControlRepositoryTest {

    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private PersonWithSignificantControlDao legalEntityDao;
    private PersonWithSignificantControlDao individualDao;

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PersonWithSignificantControlRepository personWithSignificantControlRepository;

    @BeforeEach
    void init() {
        legalEntityDao = new PersonWithSignificantControlDaoBuilder()
                .withId("legal-entity-id")
                .withTransactionId(TRANSACTION_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withLegalEntityName("Test Legal Entity")
                        .withLegalForm("Test Legal Form")
                        .build())
                .build();

        individualDao = new PersonWithSignificantControlDaoBuilder()
                .withId("individual-id")
                .withTransactionId(TRANSACTION_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withForename("John")
                        .withSurname("Doe")
                        .build())
                .build();
    }

    @AfterEach
    public void tearDown() {
        personWithSignificantControlRepository.deleteAll();
    }

    @Test
    void testInsertAndRetrieveLegalEntityPersonWithSignificantControlList() {
        personWithSignificantControlRepository.insert(legalEntityDao);

        PersonWithSignificantControlDao foundLegalEntity = personWithSignificantControlRepository.findById(legalEntityDao.getId()).orElseThrow();

        assertThat(foundLegalEntity.getData().getLegalEntityName()).isEqualTo(legalEntityDao.getData().getLegalEntityName());
        assertThat(foundLegalEntity.getData().getLegalForm()).isEqualTo(legalEntityDao.getData().getLegalForm());
    }

    @Test
    void testInsertAndRetrievePersonPersonWithSignificantControlList() {
        personWithSignificantControlRepository.insert(individualDao);

        PersonWithSignificantControlDao foundPerson = personWithSignificantControlRepository.findById(individualDao.getId()).orElseThrow();

        assertThat(foundPerson.getData().getForename()).isEqualTo(individualDao.getData().getForename());
        assertThat(foundPerson.getData().getSurname()).isEqualTo(individualDao.getData().getSurname());
    }

    @Test
    void testGetPersonWithSignificantControlListOrderedByUpdatedAtDesc() {
        personWithSignificantControlRepository.insert(individualDao);
        personWithSignificantControlRepository.insert(legalEntityDao);

        List<PersonWithSignificantControlDao> result = personWithSignificantControlRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);

        assertThat(result)
                .hasSize(2)
                .satisfiesExactly(
                        foundLegalEntity -> {
                            assertThat(foundLegalEntity.getData().getLegalEntityName()).isEqualTo(legalEntityDao.getData().getLegalEntityName());
                            assertThat(foundLegalEntity.getData().getLegalForm()).isEqualTo(legalEntityDao.getData().getLegalForm());
                        },
                        foundIndividual -> {
                            assertThat(foundIndividual.getData().getForename()).isEqualTo(individualDao.getData().getForename());
                            assertThat(foundIndividual.getData().getSurname()).isEqualTo(individualDao.getData().getSurname());
                        }
                );
    }

    @Test
    void testAuditFieldsArePopulated() {
        LocalDateTime startOfTest = LocalDateTime.now();
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlDao();
        personWithSignificantControlRepository.insert(dao);

        assertThat(dao.getCreatedAt()).isBetween(startOfTest, LocalDateTime.now());
        assertThat(dao.getUpdatedAt()).isBetween(startOfTest, LocalDateTime.now());
    }
}
