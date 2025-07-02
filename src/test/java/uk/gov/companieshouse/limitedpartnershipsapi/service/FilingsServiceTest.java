package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.GENERAL_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNER_FIELD;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class FilingsServiceTest {

    private static final String TRANSACTION_ID = "trns123";
    private static final String INCORPORATION_ID = "inc456";

    @Autowired
    private FilingsService filingsService;
    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;
    @MockitoBean
    private GeneralPartnerService generalPartnerService;
    @MockitoBean
    private LimitedPartnerService limitedPartnerService;
    @MockitoBean
    private TransactionUtils transactionUtils;


    @Test
    void testFilingGenerationSuccess() throws ServiceException {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(buildLimitedPartnership());
        when(generalPartnerService.getGeneralPartnerDataList(transaction)).thenReturn(new ArrayList<>());
        when(limitedPartnerService.getLimitedPartnerDataList(transaction)).thenReturn(new ArrayList<>());
        FilingApi filing = filingsService.generateLimitedPartnershipFiling(transaction, INCORPORATION_ID);
        assertNotNull(filing);
        assertNotNull(filing.getData());
        assertTrue(filing.getData().containsKey(LIMITED_PARTNERSHIP_FIELD));
        assertTrue(filing.getData().containsKey(GENERAL_PARTNER_FIELD));
        assertTrue(filing.getData().containsKey(LIMITED_PARTNER_FIELD));
        assertEquals("Register a Limited Partnership", filing.getDescription());
    }

    @Test
    void testFilingGenerationFailureWhenTransactionNotLinkedToIncorporation() {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(transactionUtils.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> filingsService.generateLimitedPartnershipFiling(transaction, INCORPORATION_ID));
    }

    @Test
    void testFilingGenerationFailureWhenLimitedPartnershipNotFound() throws ServiceException {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenThrow(ServiceException.class);
        assertThrows(ServiceException.class, () -> filingsService.generateLimitedPartnershipFiling(transaction, INCORPORATION_ID));
    }

    private LimitedPartnershipDto buildLimitedPartnership() {
       var limitedPartnershipDto = new LimitedPartnershipDto();
       var dataDto = new DataDto();
       limitedPartnershipDto.setData(dataDto);
       return limitedPartnershipDto;
    }
}
