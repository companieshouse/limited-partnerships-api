package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Testcontainers
@DataMongoTest
class GeneralPartnerRepositoryTest {

    private static final String TRANSACTION_ID = "transaction-123";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0.11-noble"))
            .withReuse(true);

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
        GeneralPartnerDao generalPartnerPerson = createGeneralPartnerPersonDao();
        GeneralPartnerDao generalPartnerLegalEntity = createGeneralPartnerLegalEntityDao();

        generalPartnerRepository.insert(generalPartnerPerson);
        generalPartnerRepository.insert(generalPartnerLegalEntity);

        List<GeneralPartnerDao> result = generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getData().getLegalEntityName()).isEqualTo("My company ltd");
        assertThat(result.get(0).getData().getLegalForm()).isEqualTo("Limited Company");

        assertThat(result.get(1).getData().getForename()).isEqualTo("John");
        assertThat(result.get(1).getData().getSurname()).isEqualTo("Doe");
    }

    @Test
    void testAuditFieldsArePopulated(){
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerDao();
        generalPartnerRepository.insert(generalPartnerDao);

        // Using current datetime in this test class so cannot assert actual value
        assertThat(generalPartnerDao.getCreatedAt()).isNotNull();
        assertThat(generalPartnerDao.getUpdatedAt()).isNotNull();
    }

    private GeneralPartnerDao createGeneralPartnerPersonDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();
        dao.setTransactionId(TRANSACTION_ID);
        dao.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setForename("John");
        dataDao.setSurname("Doe");
        dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
        dataDao.setNationality1("American");
        dataDao.setNotDisqualifiedStatementChecked(true);

        dao.setData(dataDao);
        dao.setId("abc-123");

        return dao;
    }

    private GeneralPartnerDao createGeneralPartnerLegalEntityDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();
        dao.setTransactionId(TRANSACTION_ID);

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");

        dao.setData(dataDao);
        dao.setId("abc-456");

        return dao;
    }
}
