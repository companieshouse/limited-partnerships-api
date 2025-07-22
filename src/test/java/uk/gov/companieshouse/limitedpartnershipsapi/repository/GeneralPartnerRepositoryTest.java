package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;

@Testcontainers
@SpringBootTest
class GeneralPartnerRepositoryTest {

    private static final String TRANSACTION_ID = "transaction-123";

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
        GeneralPartnerDao generalPartnerPerson = createGeneralPartnerPersonDao();
        GeneralPartnerDao generalPartnerLegalEntity = createGeneralPartnerLegalEntityDao();

        generalPartnerRepository.insert(generalPartnerPerson);
        generalPartnerRepository.insert(generalPartnerLegalEntity);

        List<GeneralPartnerDao> result = generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);

        assertThat(result)
                .hasSize(2)
                .satisfiesExactly(personPartner -> {
                    assertThat(personPartner.getData().getLegalEntityName()).isEqualTo("My company ltd");
                    assertThat(personPartner.getData().getLegalForm()).isEqualTo("Limited Company");
                    assertThat(personPartner.getData().getNotDisqualifiedStatementChecked()).isNull();
                }, legalEntityPartner -> {
                    assertThat(legalEntityPartner.getData().getForename()).isEqualTo("John");
                    assertThat(legalEntityPartner.getData().getSurname()).isEqualTo("Doe");
                    assertThat(legalEntityPartner.getData().getNotDisqualifiedStatementChecked()).isTrue();
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

    private GeneralPartnerDao createGeneralPartnerPersonDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();
        dao.setTransactionId(TRANSACTION_ID);

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
