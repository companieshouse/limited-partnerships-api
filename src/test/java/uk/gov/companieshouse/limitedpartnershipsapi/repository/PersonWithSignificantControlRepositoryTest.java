package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
@SpringBootTest
class PersonWithSignificantControlRepositoryTest {

    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PersonWithSignificantControlRepository personWithSignificantControlRepository;

    @AfterEach
    public void tearDown() {
        personWithSignificantControlRepository.deleteAll();
    }

    @Test
    void testInsertAndRetrieveLegalEntityPersonWithSignificantControlList() {
        PersonWithSignificantControlDao legalEntity = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

        personWithSignificantControlRepository.insert(legalEntity);

        PersonWithSignificantControlDao foundLegalEntity = personWithSignificantControlRepository.findById(legalEntity.getId()).orElseThrow();

        assertThat(foundLegalEntity.getData().getLegalEntityName()).isNotNull();
        assertThat(foundLegalEntity.getData().getLegalForm()).isNotNull();
        assertThat(foundLegalEntity.getData()).usingRecursiveComparison().isEqualTo(legalEntity.getData());
    }

    @Test
    void testInsertAndRetrievePersonPersonWithSignificantControlList() {
        PersonWithSignificantControlDao person = new PersonWithSignificantControlBuilder().individualPersonDao();

        personWithSignificantControlRepository.insert(person);

        PersonWithSignificantControlDao foundPerson = personWithSignificantControlRepository.findById(person.getId()).orElseThrow();

        assertThat(foundPerson.getData().getForename()).isNotNull();
        assertThat(foundPerson.getData().getSurname()).isNotNull();
        assertThat(foundPerson.getData()).usingRecursiveComparison().isEqualTo(person.getData());
    }

    @Test
    void testGetPersonWithSignificantControlListOrderedByUpdatedAtDesc() {
        PersonWithSignificantControlDao person = new PersonWithSignificantControlBuilder().individualPersonDao();
        PersonWithSignificantControlDao legalEntity = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();
        person.setId("782j836-922jl22-23123");
        legalEntity.setId("8014b4-897pu76-9976");

        person.setTransactionId(TRANSACTION_ID);
        legalEntity.setTransactionId(TRANSACTION_ID);

        personWithSignificantControlRepository.insert(person);
        personWithSignificantControlRepository.insert(legalEntity);

        List<PersonWithSignificantControlDao> result = personWithSignificantControlRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);

        assertThat(result)
                .hasSize(2)
                .satisfiesExactly(
                        foundLegalEntity -> {
                            assertThat(foundLegalEntity.getData().getLegalEntityName()).isNotNull();
                            assertThat(foundLegalEntity.getData().getLegalForm()).isNotNull();
                            assertThat(foundLegalEntity.getData()).usingRecursiveComparison().isEqualTo(legalEntity.getData());
                        },
                        foundPerson -> {
                            assertThat(foundPerson.getData().getForename()).isNotNull();
                            assertThat(foundPerson.getData().getSurname()).isNotNull();
                            assertThat(foundPerson.getData()).usingRecursiveComparison().isEqualTo(person.getData());
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
