package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.GENERAL_PARTNER_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNER_FIELD;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class FilingsServiceTest {

    private static String TRANSACTION_ID = "trns123";
    @Autowired
    private FilingsService filingsService;
    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;
    @MockitoBean
    private GeneralPartnerService generalPartnerService;
    @MockitoBean
    private LimitedPartnerService limitedPartnerService;

    @Test
    void testFilingGenerationSuccess() throws ServiceException {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(buildLimitedPartnership());
        when(generalPartnerService.getGeneralPartnerList(transaction)).thenReturn(buildGeneralPartnerDtoList());
        when(limitedPartnerService.getLimitedPartnerList(transaction)).thenReturn(buildLimitedPartnerDtoList());
        FilingApi filing = filingsService.generateLimitedPartnerFilings(transaction);
        assertNotNull(filing);
        assertNotNull(filing.getData());
        assertTrue(filing.getData().containsKey(LIMITED_PARTNERSHIP_FIELD));
        assertTrue(filing.getData().containsKey(GENERAL_PARTNER_FIELD));
        assertTrue(filing.getData().containsKey(LIMITED_PARTNER_FIELD));
    }

    @Test
    void testFilingGenerationWithException() throws ServiceException {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenThrow(ServiceException.class);
        assertThrows(ServiceException.class, () -> filingsService.generateLimitedPartnerFilings(transaction));
    }

    private LimitedPartnershipDto buildLimitedPartnership() {
       var limitedPartnershipDto = new LimitedPartnershipDto();
       var dataDto = new DataDto();
       limitedPartnershipDto.setData(dataDto);
       return limitedPartnershipDto;
    }

    private List<GeneralPartnerDto> buildGeneralPartnerDtoList()
    {
        return null;
    }

    private List<LimitedPartnerDto> buildLimitedPartnerDtoList()
    {
        return null;
    }
}
