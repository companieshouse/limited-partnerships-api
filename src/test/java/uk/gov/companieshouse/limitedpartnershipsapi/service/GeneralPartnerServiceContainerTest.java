package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;

import static org.junit.Assert.assertEquals;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@Testcontainers
@SpringBootTest
public class GeneralPartnerServiceContainerTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = Containers.mongoDBContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    Transaction transaction = new TransactionBuilder().forPartner(
                    FILING_KIND_GENERAL_PARTNER,
                    URL_GET_GENERAL_PARTNER,
                    GeneralPartnerBuilder.GENERAL_PARTNER_ID
            )
            .build();

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Autowired
    private GeneralPartnerService service;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    public void createGeneralPartnerLegalEntityPostTransition() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        transaction.setFilingMode(IncorporationKind.POST_TRANSITION.getDescription());

        GeneralPartnerDto dto = new GeneralPartnerBuilder().legalEntityDto();
        dto.getData().setKind(PartnerKind.ADD_GENERAL_PARTNER_LEGAL_ENTITY.getDescription());

        var id = service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

        GeneralPartnerDto generalPartnerDto = service.getGeneralPartner(transaction, id);

        assertEquals(PartnerKind.ADD_GENERAL_PARTNER_LEGAL_ENTITY.getDescription(), generalPartnerDto.getData().getKind());
    }
}
