package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PscRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PSC;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PSC;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PscServiceUpdateTest {
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String USER_ID = "xbJf0l";
    private static final String PSC_ID = PscBuilder.ID;

    private final Transaction transaction = new TransactionBuilder().withKindAndUri(
            FILING_KIND_PSC,
            URL_GET_PSC,
            PSC_ID
    ).build();

    @Autowired
    private PscService pscService;

    @MockitoBean
    private PscRepository pscRepository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<PscDao> pscDaoArgumentCaptor;


    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException {
        PscDao pscDao = new PscBuilder.PscDaoBuilder()
                .legalEntityPscDao()
                .withNationality2(Nationality.GREENLANDIC.getDescription())
                .withPrincipalOfficeAddress(null)
                .build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder().legalEntityPscDto().build().getData();

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(pscRepository.findById(pscDao.getId())).thenReturn(Optional.of(pscDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(pscDao.getData().getPrincipalOfficeAddress());
        assertNotNull(pscDataDto.getPrincipalOfficeAddress());

        // dto nationality2 should be null
        assertNull(pscDataDto.getNationality2());

        pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID);

        verify(pscRepository).findById(PSC_ID);
        verify(pscRepository).save(pscDaoArgumentCaptor.capture());

        PscDao savedPscDao = pscDaoArgumentCaptor.getValue();

        AddressDto principalOfficeAddress = pscDataDto.getPrincipalOfficeAddress();
        AddressDao savedPrincipalOfficeAddress = savedPscDao.getData().getPrincipalOfficeAddress();

        assertEquals(pscDataDto.getForename(), savedPscDao.getData().getForename());
        assertEquals(principalOfficeAddress.getAddressLine1(), savedPrincipalOfficeAddress.getAddressLine1());
        assertEquals(principalOfficeAddress.getAddressLine2(), savedPrincipalOfficeAddress.getAddressLine2());
        assertEquals(principalOfficeAddress.getCountry(), savedPrincipalOfficeAddress.getCountry());
        assertEquals(principalOfficeAddress.getLocality(), savedPrincipalOfficeAddress.getLocality());
        assertEquals(principalOfficeAddress.getPostalCode(), savedPrincipalOfficeAddress.getPostalCode());
        assertEquals(principalOfficeAddress.getPremises(), savedPrincipalOfficeAddress.getPremises());
        assertEquals(principalOfficeAddress.getRegion(), savedPrincipalOfficeAddress.getRegion());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), savedPscDao.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithUsualResidentialAddress() throws ServiceException {
        PscDao pscDao = new PscBuilder.PscDaoBuilder()
                .personPscDao()
                .withNationality2(Nationality.GREENLANDIC.getDescription())
                .withUsualResidentialAddress(null)
                .build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder()
                .personPscDto()
                .withNationality1(null)
                .withNationality2(null)
                .build()
                .getData();

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(pscRepository.findById(pscDao.getId())).thenReturn(Optional.of(pscDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao usual residential address is null before mapping/update
        assertNull(pscDao.getData().getUsualResidentialAddress());
        assertNotNull(pscDataDto.getUsualResidentialAddress());

        pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID);

        verify(pscRepository).findById(PSC_ID);
        verify(pscRepository).save(pscDaoArgumentCaptor.capture());

        PscDao savedPscDao = pscDaoArgumentCaptor.getValue();

        AddressDto patchUsualResidentialAddress = pscDataDto.getUsualResidentialAddress();
        AddressDao savedUsualResidentialAddress = savedPscDao.getData().getUsualResidentialAddress();

        assertEquals(pscDataDto.getForename(), savedPscDao.getData().getForename());
        assertEquals(patchUsualResidentialAddress.getAddressLine1(), savedUsualResidentialAddress.getAddressLine1());
        assertEquals(patchUsualResidentialAddress.getAddressLine2(), savedUsualResidentialAddress.getAddressLine2());
        assertEquals(patchUsualResidentialAddress.getCountry(), savedUsualResidentialAddress.getCountry());
        assertEquals(patchUsualResidentialAddress.getLocality(), savedUsualResidentialAddress.getLocality());
        assertEquals(patchUsualResidentialAddress.getPostalCode(), savedUsualResidentialAddress.getPostalCode());
        assertEquals(patchUsualResidentialAddress.getPremises(), savedUsualResidentialAddress.getPremises());
        assertEquals(patchUsualResidentialAddress.getRegion(), savedUsualResidentialAddress.getRegion());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), savedPscDao.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithServiceAddress() throws ServiceException {
        PscDao pscDao = new PscBuilder.PscDaoBuilder()
                .personPscDao()
                .withNationality2(Nationality.GREENLANDIC.getDescription())
                .withServiceAddress(null)
                .build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder()
                .personPscDto()
                .withNationality1(null)
                .withNationality2(null)
                .build()
                .getData();

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(pscRepository.findById(pscDao.getId())).thenReturn(Optional.of(pscDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao usual residential address is null before mapping/update
        assertNull(pscDao.getData().getServiceAddress());
        assertNotNull(pscDataDto.getServiceAddress());

        pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID);

        verify(pscRepository).findById(PSC_ID);
        verify(pscRepository).save(pscDaoArgumentCaptor.capture());

        PscDao savedPscDao = pscDaoArgumentCaptor.getValue();

        AddressDto patchServiceAddress = pscDataDto.getServiceAddress();
        AddressDao savedServiceAddress = savedPscDao.getData().getServiceAddress();

        assertEquals(pscDataDto.getForename(), savedPscDao.getData().getForename());
        assertEquals(patchServiceAddress.getAddressLine1(), savedServiceAddress.getAddressLine1());
        assertEquals(patchServiceAddress.getAddressLine2(), savedServiceAddress.getAddressLine2());
        assertEquals(patchServiceAddress.getCountry(), savedServiceAddress.getCountry());
        assertEquals(patchServiceAddress.getLocality(), savedServiceAddress.getLocality());
        assertEquals(patchServiceAddress.getPostalCode(), savedServiceAddress.getPostalCode());
        assertEquals(patchServiceAddress.getPremises(), savedServiceAddress.getPremises());
        assertEquals(patchServiceAddress.getRegion(), savedServiceAddress.getRegion());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), savedPscDao.getData().getNationality2());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        PscDao pscDao = new PscBuilder.PscDaoBuilder().personPscDao().build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder().personPscDto().build().getData();
        pscDataDto.setNationality1(Nationality.AMERICAN);
        pscDataDto.setNationality2(null);

        transaction.setFilingMode(FilingMode.TRANSITION.getDescription());

        when(pscRepository.findById(pscDao.getId())).thenReturn(Optional.of(pscDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID);

        verify(pscRepository).save(pscDaoArgumentCaptor.capture());

        PscDao savedPscDao = pscDaoArgumentCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), savedPscDao.getData().getNationality1());
        assertNull(savedPscDao.getData().getNationality2());
    }

    @Test
    void shouldThrowExceptionIfNotLinkedToTransaction() {
        PscDao pscDao = new PscBuilder.PscDaoBuilder().personPscDao().build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder().personPscDto().build().getData();
        pscDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(pscRepository.findById(PSC_ID)).thenReturn(Optional.of(pscDao));

        assertThatThrownBy(() -> pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", transaction.getId(), PSC_ID));
    }
}
