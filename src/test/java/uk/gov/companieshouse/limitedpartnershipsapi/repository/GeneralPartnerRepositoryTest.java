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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
@SpringBootTest
class GeneralPartnerRepositoryTest {

    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private GeneralPartnerRepository generalPartnerRepository;

    @AfterEach
    public void tearDown() {
        generalPartnerRepository.deleteAll();
    }

    @Test
    void testGetGeneralPartnerListOrderedByUpdatedAtDesc() {
        GeneralPartnerDao generalPartnerPerson = new GeneralPartnerBuilder().personDao();
        GeneralPartnerDao generalPartnerLegalEntity = new GeneralPartnerBuilder().legalEntityDao();
        generalPartnerLegalEntity.setId("8014b46c-29f6-4f42-a2b9-9ba512e0be4b");

        generalPartnerRepository.insert(generalPartnerPerson);
        generalPartnerRepository.insert(generalPartnerLegalEntity);

        List<GeneralPartnerDao> result = generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);

        assertThat(result)
                .hasSize(2)
                .satisfiesExactly(legalEntityPartner -> {
                            AssertionsForClassTypes.assertThat(legalEntityPartner.getData().getLegalEntityName()).isEqualTo(generalPartnerLegalEntity.getData().getLegalEntityName());
                            AssertionsForClassTypes.assertThat(legalEntityPartner.getData().getLegalForm()).isEqualTo(generalPartnerLegalEntity.getData().getLegalForm());
                            AssertionsForClassTypes.assertThat(legalEntityPartner.getData().getNotDisqualifiedStatementChecked()).isTrue();
                        },
                        personPartner -> {
                            AssertionsForClassTypes.assertThat(personPartner.getData().getForename()).isEqualTo(generalPartnerPerson.getData().getForename());
                            AssertionsForClassTypes.assertThat(personPartner.getData().getSurname()).isEqualTo(generalPartnerPerson.getData().getSurname());
                            AssertionsForClassTypes.assertThat(personPartner.getData().getNotDisqualifiedStatementChecked()).isTrue();
                        });
    }

    @Test
    void testAuditFieldsArePopulated() {
        LocalDateTime startOfTest = LocalDateTime.now();
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerDao();
        generalPartnerRepository.insert(generalPartnerDao);

        // Using current datetime in this test class so cannot assert actual value
        assertThat(generalPartnerDao.getCreatedAt()).isBetween(startOfTest, LocalDateTime.now());
        assertThat(generalPartnerDao.getUpdatedAt()).isBetween(startOfTest, LocalDateTime.now());
    }
}
