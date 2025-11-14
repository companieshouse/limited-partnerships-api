package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.Containers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@Testcontainers
@SpringBootTest
class GeneralPartnerServiceContainerTest {

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

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CompanyProfileApi companyProfileApi;

    @ParameterizedTest
    @EnumSource(PartnerKind.class)
    void createGeneralPartnerLegalEntityPostTransition(PartnerKind partnerKind) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        transaction.setFilingMode(FilingMode.POST_TRANSITION.getDescription());

        GeneralPartnerDto dto = new GeneralPartnerBuilder().legalEntityDto();
        dto.setId(null);
        dto.getData().setKind(partnerKind.getDescription());
        dto.getData().setCeaseDate(LocalDate.of(2025, 1, 1));

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);
        when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
        when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2022, 1, 3));

        var id = service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

        GeneralPartnerDto generalPartnerDto = service.getGeneralPartner(transaction, id);

        assertEquals(partnerKind.getDescription(), generalPartnerDto.getData().getKind());
    }

    @Test
    void createGeneralPartnerLegalEntityTransition() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        GeneralPartnerDto dto = new GeneralPartnerBuilder().legalEntityDto();
        dto.setId(null);

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);
        when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);
        when(companyProfileApi.getDateOfCreation()).thenReturn(LocalDate.of(2022, 1, 3));

        var id = service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

        GeneralPartnerDto generalPartnerDto = service.getGeneralPartner(transaction, id);

        assertEquals(FILING_KIND_GENERAL_PARTNER, generalPartnerDto.getData().getKind());
    }
}
