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
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.ArrayList;
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
        when(generalPartnerService.getGeneralPartnerDataList(transaction)).thenReturn(buildGeneralPartnerDataDtoList());
        when(limitedPartnerService.getLimitedPartnerDataList(transaction)).thenReturn(buildLimitedPartnerDataDtoList());
        FilingApi filing = filingsService.generateLimitedPartnerFiling(transaction);
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
        assertThrows(ServiceException.class, () -> filingsService.generateLimitedPartnerFiling(transaction));
    }

    private LimitedPartnershipDto buildLimitedPartnership() {
       var limitedPartnershipDto = new LimitedPartnershipDto();
       var dataDto = new DataDto();
       limitedPartnershipDto.setData(dataDto);
       return limitedPartnershipDto;
    }

    private List<GeneralPartnerDataDto> buildGeneralPartnerDataDtoList() {
        List<GeneralPartnerDataDto> dtoDataList = new ArrayList<>();
        GeneralPartnerDataDto data1 = new GeneralPartnerDataDto();
        dtoDataList.add(data1);
        GeneralPartnerDataDto data2 = new GeneralPartnerDataDto();
        dtoDataList.add(data2);
        GeneralPartnerDataDto data3 = new GeneralPartnerDataDto();
        dtoDataList.add(data3);
        GeneralPartnerDataDto data4 = new GeneralPartnerDataDto();
        dtoDataList.add(data4);
        return dtoDataList;
    }

    private List<LimitedPartnerDataDto> buildLimitedPartnerDataDtoList() {
        List<LimitedPartnerDataDto> dtoDataList = new ArrayList<>();
        LimitedPartnerDataDto data1 = new LimitedPartnerDataDto();
        dtoDataList.add(data1);
        LimitedPartnerDataDto data2 = new LimitedPartnerDataDto();
        dtoDataList.add(data2);
        LimitedPartnerDataDto data3 = new LimitedPartnerDataDto();
        dtoDataList.add(data3);
        LimitedPartnerDataDto data4 = new LimitedPartnerDataDto();
        dtoDataList.add(data4);
        return dtoDataList;
    }
}
