package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
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


    @BeforeEach
    void setUp() {
        transaction.setFilingMode(FilingMode.REGISTRATION.getDescription());
    }

    private void assertAddressEquals(AddressDto expected, AddressDao actual) {
        assertThat(actual).isNotNull();
        assertThat(actual.getAddressLine1()).isEqualTo(expected.getAddressLine1());
        assertThat(actual.getAddressLine2()).isEqualTo(expected.getAddressLine2());
        assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
        assertThat(actual.getLocality()).isEqualTo(expected.getLocality());
        assertThat(actual.getPostalCode()).isEqualTo(expected.getPostalCode());
        assertThat(actual.getPremises()).isEqualTo(expected.getPremises());
        assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
    }

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException {
        PscDao pscDao = new PscBuilder.PscDaoBuilder()
                .legalEntityPscDao()
                .withPrincipalOfficeAddress(null)
                .build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder().legalEntityPscDto().build().getData();

        when(pscRepository.findById(pscDao.getId())).thenReturn(Optional.of(pscDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(pscDao.getData().getPrincipalOfficeAddress());
        assertNotNull(pscDataDto.getPrincipalOfficeAddress());

        pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID);

        verify(pscRepository).findById(PSC_ID);
        verify(pscRepository).save(pscDaoArgumentCaptor.capture());

        PscDao savedPscDao = pscDaoArgumentCaptor.getValue();

        AddressDto principalOfficeAddress = pscDataDto.getPrincipalOfficeAddress();
        AddressDao savedPrincipalOfficeAddress = savedPscDao.getData().getPrincipalOfficeAddress();

        assertEquals(pscDataDto.getForename(), savedPscDao.getData().getForename());
        assertAddressEquals(principalOfficeAddress, savedPrincipalOfficeAddress);
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
        assertAddressEquals(patchUsualResidentialAddress, savedUsualResidentialAddress);
        assertThat(savedPscDao.getData().getNationality2()).isEqualTo(Nationality.GREENLANDIC.getDescription());
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
        assertAddressEquals(patchServiceAddress, savedServiceAddress);
        assertThat(savedPscDao.getData().getNationality2()).isEqualTo(Nationality.GREENLANDIC.getDescription());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        PscDao pscDao = new PscBuilder.PscDaoBuilder().personPscDao().build();

        PscDataDto pscDataDto = new PscBuilder.PscDtoBuilder().personPscDto().build().getData();
        pscDataDto.setNationality1(Nationality.AMERICAN);
        pscDataDto.setNationality2(null);

        when(pscRepository.findById(pscDao.getId())).thenReturn(Optional.of(pscDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        pscService.updatePsc(transaction, PSC_ID, pscDataDto, REQUEST_ID, USER_ID);

        verify(pscRepository).save(pscDaoArgumentCaptor.capture());

        PscDao savedPscDao = pscDaoArgumentCaptor.getValue();

        assertThat(savedPscDao.getData().getNationality1()).isEqualTo(Nationality.AMERICAN.getDescription());
        assertThat(savedPscDao.getData().getNationality2()).isNull();
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
