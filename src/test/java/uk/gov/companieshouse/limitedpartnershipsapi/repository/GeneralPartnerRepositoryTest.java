package uk.gov.companieshouse.limitedpartnershipsapi.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Disabled("Disabled until we have a test container for MongoDB")
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class GeneralPartnerRepositoryTest {
    private static final String TRANSACTION_ID = "transaction-123";

    @Autowired
    private GeneralPartnerRepository generalPartnerRepository;

    @AfterEach
    public void tearDown() {
        generalPartnerRepository.deleteAll();
    }

    @Test
    public void testGetGeneralPartnerListOrderedByUpdatedAtDesc() {
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
        dao.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 0, 0));

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");
        dataDao.setLegalPersonalityStatementChecked(true);

        dao.setData(dataDao);
        dao.setId("abc-456");

        return dao;
    }
}
