package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.AddressDtoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDtoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PersonWithSignificantControlServiceUpdateTest {
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String USER_ID = "xbJf0l";
    private static final String PSC_ID = "psc123";
    private static final String LEGAL_ENTITY_NAME = "Test Legal Entity";
    private static final String LEGAL_FORM = "Test Legal Form";
    private static final String GOVERNING_LAW = "Test Governing Law";
    private static final String ADDRESS_LINE_1 = "123 Street St";
    private static final String PREMISES = "Premises";
    private static final String LOCALITY = "City";
    private static final String POSTAL_CODE = "PR121TG";
    private static final String FORENAME = "John";
    private static final String SURNAME = "Smith";
    private static final String BRITISH = "British";

    private final Transaction transaction = new TransactionBuilder().withKindAndUri(
            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
            URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL,
            PSC_ID
    ).build();

    @Autowired
    private PersonWithSignificantControlService personWithSignificantControlService;

    @MockitoBean
    private PersonWithSignificantControlRepository personWithSignificantControlRepository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<PersonWithSignificantControlDao> pscDaoArgumentCaptor;

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withLegalEntityName(LEGAL_ENTITY_NAME)
                        .withLegalForm(LEGAL_FORM)
                        .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                        .build())
                .build();

        PersonWithSignificantControlDataDto legalEntityPersonWithSignificantControlDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withLegalEntityName(LEGAL_ENTITY_NAME)
                .withLegalForm(LEGAL_FORM)
                .withGoverningLaw(GOVERNING_LAW)
                .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                .withType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY)
                .withPrincipalOfficeAddress(new AddressDtoBuilder()
                        .withAddressLine1(ADDRESS_LINE_1)
                        .withPremises(PREMISES)
                        .withLocality(LOCALITY)
                        .withPostalCode(POSTAL_CODE)
                        .withCountry(Country.ENGLAND.getDescription())
                        .build())
                .build();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(personWithSignificantControlDao.getData().getPrincipalOfficeAddress());
        assertNotNull(legalEntityPersonWithSignificantControlDataDto.getPrincipalOfficeAddress());

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, legalEntityPersonWithSignificantControlDataDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).findById(PSC_ID);
        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        AddressDto principalOfficeAddress = legalEntityPersonWithSignificantControlDataDto.getPrincipalOfficeAddress();
        AddressDao savedPrincipalOfficeAddress = savedPersonWithSignificantControlDao.getData().getPrincipalOfficeAddress();

        assertEquals(legalEntityPersonWithSignificantControlDataDto.getForename(), savedPersonWithSignificantControlDao.getData().getForename());
        assertAddressEquals(principalOfficeAddress, savedPrincipalOfficeAddress);
    }

    @Test
    void shouldUpdateTheDaoWithUsualResidentialAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withForename(FORENAME)
                        .withSurname(SURNAME)
                        .withNationality1(BRITISH)
                        .withNationality2(Nationality.GREENLANDIC.getDescription())
                        .withUsualResidentialAddress(null)
                        .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                        .build())
                .build();

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withForename(FORENAME)
                .withSurname(SURNAME)
                .withNationality1(null)
                .withNationality2(null)
                .withType(PersonWithSignificantControlType.INDIVIDUAL_PERSON)
                .withUsualResidentialAddress(new AddressDtoBuilder()
                        .withAddressLine1(ADDRESS_LINE_1)
                        .withLocality(LOCALITY)
                        .withPostalCode(POSTAL_CODE)
                        .withCountry(Country.ENGLAND.getDescription())
                        .build())
                .build();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao usual residential address is null before mapping/update
        assertNull(personWithSignificantControlDao.getData().getUsualResidentialAddress());
        assertNotNull(personWithSignificantControlDataDto.getUsualResidentialAddress());

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).findById(PSC_ID);
        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        AddressDto patchUsualResidentialAddress = personWithSignificantControlDataDto.getUsualResidentialAddress();
        AddressDao savedUsualResidentialAddress = savedPersonWithSignificantControlDao.getData().getUsualResidentialAddress();

        assertEquals(personWithSignificantControlDataDto.getForename(), savedPersonWithSignificantControlDao.getData().getForename());
        assertAddressEquals(patchUsualResidentialAddress, savedUsualResidentialAddress);
        assertThat(savedPersonWithSignificantControlDao.getData().getNationality2()).isEqualTo(Nationality.GREENLANDIC.getDescription());
    }

    @Test
    void shouldUpdateTheDaoWithServiceAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withForename(FORENAME)
                        .withSurname(SURNAME)
                        .withNationality1(BRITISH)
                        .withNationality2(Nationality.GREENLANDIC.getDescription())
                        .withServiceAddress(null)
                        .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                        .withType(PersonWithSignificantControlType.INDIVIDUAL_PERSON)
                        .build())
                .build();

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withForename(FORENAME)
                .withSurname(SURNAME)
                .withNationality1(null)
                .withNationality2(null)
                .withServiceAddress(new AddressDtoBuilder()
                        .withAddressLine1(ADDRESS_LINE_1)
                        .withLocality(LOCALITY)
                        .withPostalCode(POSTAL_CODE)
                        .withCountry(Country.ENGLAND.getDescription())
                        .build())
                .build();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao usual residential address is null before mapping/update
        assertNull(personWithSignificantControlDao.getData().getServiceAddress());
        assertNotNull(personWithSignificantControlDataDto.getServiceAddress());

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).findById(PSC_ID);
        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        AddressDto patchServiceAddress = personWithSignificantControlDataDto.getServiceAddress();
        AddressDao savedServiceAddress = savedPersonWithSignificantControlDao.getData().getServiceAddress();

        assertEquals(personWithSignificantControlDataDto.getForename(), savedPersonWithSignificantControlDao.getData().getForename());
        assertAddressEquals(patchServiceAddress, savedServiceAddress);
        assertThat(savedPersonWithSignificantControlDao.getData().getNationality2()).isEqualTo(Nationality.GREENLANDIC.getDescription());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withForename(FORENAME)
                        .withSurname(SURNAME)
                        .withNationality1(BRITISH)
                        .withNationality2("English")
                        .build())
                .build();

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withForename(FORENAME)
                .withSurname(SURNAME)
                .withNationality1(Nationality.AMERICAN)
                .withNationality2(null)
                .withType(PersonWithSignificantControlType.INDIVIDUAL_PERSON)
                .build();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        assertThat(savedPersonWithSignificantControlDao.getData().getNationality1()).isEqualTo(Nationality.AMERICAN.getDescription());
        assertThat(savedPersonWithSignificantControlDao.getData().getNationality2()).isNull();
    }

    @Test
    void shouldThrowExceptionIfNotLinkedToTransaction() {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withForename(FORENAME)
                        .withSurname(SURNAME)
                        .build())
                .build();

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlDtoBuilder.DataBuilder()
                .withForename(FORENAME)
                .withSurname(SURNAME)
                .build();

        when(personWithSignificantControlRepository.findById(PSC_ID)).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", transaction.getId(), PSC_ID));
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
}
