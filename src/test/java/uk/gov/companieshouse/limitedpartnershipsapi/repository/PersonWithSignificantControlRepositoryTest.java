package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.assertj.core.api.AssertionsForClassTypes;
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
        PersonWithSignificantControlDao legalEntity = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().legalEntityPersonWithSignificantControlDao().build();

        personWithSignificantControlRepository.insert(legalEntity);

        PersonWithSignificantControlDao foundLegalEntity = personWithSignificantControlRepository.findById(legalEntity.getId()).orElseThrow();

        AssertionsForClassTypes.assertThat(foundLegalEntity.getData().getLegalEntityName()).isEqualTo(legalEntity.getData().getLegalEntityName());
        AssertionsForClassTypes.assertThat(foundLegalEntity.getData().getLegalForm()).isEqualTo(legalEntity.getData().getLegalForm());
    }

    @Test
    void testInsertAndRetrievePersonPersonWithSignificantControlList() {
        PersonWithSignificantControlDao person = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personPersonWithSignificantControlDao().build();

        personWithSignificantControlRepository.insert(person);

        PersonWithSignificantControlDao foundPerson = personWithSignificantControlRepository.findById(person.getId()).orElseThrow();

        AssertionsForClassTypes.assertThat(foundPerson.getData().getForename()).isEqualTo(person.getData().getForename());
        AssertionsForClassTypes.assertThat(foundPerson.getData().getSurname()).isEqualTo(person.getData().getSurname());
    }

    @Test
    void testGetPersonWithSignificantControlListOrderedByUpdatedAtDesc() {
        PersonWithSignificantControlDao person = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().personPersonWithSignificantControlDao().build();
        PersonWithSignificantControlDao legalEntity = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDaoBuilder().legalEntityPersonWithSignificantControlDao().build();
        legalEntity.setId("8014b46c-29f6-4f42-a2b9-9ba512e0be4b");
        person.setTransactionId(TRANSACTION_ID);
        legalEntity.setTransactionId(TRANSACTION_ID);

        personWithSignificantControlRepository.insert(person);
        personWithSignificantControlRepository.insert(legalEntity);

        List<PersonWithSignificantControlDao> result = personWithSignificantControlRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);

        assertThat(result)
                .hasSize(2)
                .satisfiesExactly(
                        entity -> {
                            AssertionsForClassTypes.assertThat(entity.getData().getLegalEntityName()).isEqualTo(legalEntity.getData().getLegalEntityName());
                            AssertionsForClassTypes.assertThat(entity.getData().getLegalForm()).isEqualTo(legalEntity.getData().getLegalForm());
                        },
                        p -> {
                            AssertionsForClassTypes.assertThat(p.getData().getForename()).isEqualTo(person.getData().getForename());
                            AssertionsForClassTypes.assertThat(p.getData().getSurname()).isEqualTo(person.getData().getSurname());
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
