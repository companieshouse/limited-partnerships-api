package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    private static final String PSC_ID = PersonWithSignificantControlBuilder.PERSON_WITH_SIGNIFICANT_CONTROL_ID;

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
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();
        personWithSignificantControlDao.getData().setPrincipalOfficeAddress(null);

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlBuilder().relevantLegalEntityDto().getData();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // dao principal office address is null before mapping/update
        assertNull(personWithSignificantControlDao.getData().getPrincipalOfficeAddress());
        assertNotNull(personWithSignificantControlDataDto.getPrincipalOfficeAddress());

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).findById(PSC_ID);
        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        AddressDto principalOfficeAddress = personWithSignificantControlDataDto.getPrincipalOfficeAddress();
        AddressDao savedPrincipalOfficeAddress = savedPersonWithSignificantControlDao.getData().getPrincipalOfficeAddress();

        assertEquals(personWithSignificantControlDataDto.getForename(), savedPersonWithSignificantControlDao.getData().getForename());
        assertAddressEquals(principalOfficeAddress, savedPrincipalOfficeAddress);
    }

    @Test
    void shouldUpdateTheDaoWithUsualResidentialAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();
        personWithSignificantControlDao.getData().setUsualResidentialAddress(null);

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlBuilder().individualPersonDto().getData();

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

        assertAddressEquals(patchUsualResidentialAddress, savedUsualResidentialAddress);
    }

    @Test
    void shouldUpdateTheDaoWithServiceAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();
        personWithSignificantControlDao.getData().setServiceAddress(null);

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlBuilder().individualPersonDto().getData();

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

        assertAddressEquals(patchServiceAddress, savedServiceAddress);
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlBuilder()
                .withNationality1(Nationality.AMERICAN)
                .withNationality2(null)
                .individualPersonDto().getData();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        assertThat(savedPersonWithSignificantControlDao.getData().getNationality1()).isEqualTo(Nationality.AMERICAN.getDescription());
        assertThat(savedPersonWithSignificantControlDao.getData().getNationality2()).isNull();
    }

    @Test
    void shouldHandleLegalEntityRegistrationLocationOptionality_shouldNotDelete() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

        var preChangeLegalEntityRegistrationLocation = personWithSignificantControlDao.getData().getLegalEntityRegistrationLocation();
        assertThat(preChangeLegalEntityRegistrationLocation).isNotEmpty();

        PersonWithSignificantControlDataDto changesDto = new PersonWithSignificantControlDataDto();
        changesDto.setNaturesOfControl(List.of(NatureOfControl.INDIVIDUAL_TRUST_CONTROL));

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, changesDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());
        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();
        assertThat(savedPersonWithSignificantControlDao.getData().getLegalEntityRegistrationLocation()).isEqualTo(preChangeLegalEntityRegistrationLocation);
    }

    @Test
    void shouldHandleLegalEntityRegistrationLocationOptionality_shouldDelete() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

        var preChangeLegalEntityRegistrationLocation = personWithSignificantControlDao.getData().getLegalEntityRegistrationLocation();
        assertThat(preChangeLegalEntityRegistrationLocation).isNotEmpty();

        PersonWithSignificantControlDataDto changesDto = new PersonWithSignificantControlBuilder()
                .withLegalEntityRegistrationLocation(null)
                .relevantLegalEntityDto().getData();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, changesDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());
        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();
        assertThat(savedPersonWithSignificantControlDao.getData().getLegalEntityRegistrationLocation()).isNull();
    }

    @Test
    void shouldHandleIndividualPersonOptionalFields_shouldNotDelete() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        var preChangeTitle = personWithSignificantControlDao.getData().getTitle();
        assertThat(preChangeTitle).isNotEmpty();

        var preChangeMiddleNames = personWithSignificantControlDao.getData().getMiddleNames();
        assertThat(preChangeMiddleNames).isNotEmpty();

        PersonWithSignificantControlDataDto changesDto = new PersonWithSignificantControlDataDto();
        changesDto.setNaturesOfControl(List.of(NatureOfControl.INDIVIDUAL_TRUST_CONTROL));

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, changesDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());
        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();
        assertThat(savedPersonWithSignificantControlDao.getData().getTitle()).isEqualTo(preChangeTitle);
        assertThat(savedPersonWithSignificantControlDao.getData().getMiddleNames()).isEqualTo(preChangeMiddleNames);
    }

    @Test
    void shouldHandleIndividualPersonOptionalFields_shouldDelete() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        var preChangeTitle = personWithSignificantControlDao.getData().getTitle();
        assertThat(preChangeTitle).isNotEmpty();

        var preChangeMiddleNames = personWithSignificantControlDao.getData().getMiddleNames();
        assertThat(preChangeMiddleNames).isNotEmpty();

        PersonWithSignificantControlDataDto changesDto = new PersonWithSignificantControlBuilder()
                .withForename("John")
                .withSurname("Doe")
                .withTitle(null)
                .withMiddleNames(null)
                .individualPersonDto().getData();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, changesDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());
        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();
        assertThat(savedPersonWithSignificantControlDao.getData().getTitle()).isNull();
        assertThat(savedPersonWithSignificantControlDao.getData().getMiddleNames()).isNull();
    }

    @Test
    void shouldThrowExceptionIfNotLinkedToTransaction() {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlBuilder().individualPersonDto().getData();

        when(personWithSignificantControlRepository.findById(PSC_ID)).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDataDto, REQUEST_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", transaction.getId(), PSC_ID));
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectTypeUpdateCases")
    void shouldReturnErrorIfUpdateAttemptedWithIncorrectType(
            PersonWithSignificantControlDao existingDao,
            PersonWithSignificantControlDataDto dto,
            PersonWithSignificantControlType incorrectType
    ) {
        dto.setType(incorrectType);

        when(personWithSignificantControlRepository.findById(PSC_ID)).thenReturn(Optional.of(existingDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> personWithSignificantControlService.updatePersonWithSignificantControl(
                transaction, PSC_ID, dto, REQUEST_ID, USER_ID))
                .isInstanceOf(MethodArgumentNotValidException.class)
                .hasMessageContaining("Person with significant control type cannot be changed");
    }

    private static Stream<Arguments> provideIncorrectTypeUpdateCases() {
        return Stream.of(
                Arguments.of(
                        new PersonWithSignificantControlBuilder().individualPersonDao(),
                        new PersonWithSignificantControlBuilder().individualPersonDto().getData(),
                        PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY
                ),
                Arguments.of(
                        new PersonWithSignificantControlBuilder().relevantLegalEntityDao(),
                        new PersonWithSignificantControlBuilder().relevantLegalEntityDto().getData(),
                        PersonWithSignificantControlType.OTHER_REGISTRABLE_PERSON
                ),
                Arguments.of(
                        new PersonWithSignificantControlBuilder().otherRegistrablePersonDao(),
                        new PersonWithSignificantControlBuilder().otherRegistrablePersonDto().getData(),
                        PersonWithSignificantControlType.INDIVIDUAL_PERSON
                )
        );
    }

    @Test
    void shouldUpdateTheDaoWithNatureOfControlTypesInOrder() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        PersonWithSignificantControlDataDto changesDto = new PersonWithSignificantControlDataDto();
        changesDto.setNatureOfControlTypes(List.of(NatureOfControlType.INDIVIDUAL, NatureOfControlType.TRUST, NatureOfControlType.FIRM));

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, changesDto, REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());
        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();
        assertThat(savedPersonWithSignificantControlDao.getData().getNatureOfControlTypes()).isEqualTo(List.of(NatureOfControlType.INDIVIDUAL.toString(), NatureOfControlType.FIRM.toString(), NatureOfControlType.TRUST.toString()));
    }

    @Test
    void shouldNotUpdateTheDaoWithAWrongValueForNatureOfControlTypes() {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        PersonWithSignificantControlDataDto changesDto = new PersonWithSignificantControlDataDto();
        changesDto.setNatureOfControlTypes(List.of(NatureOfControlType.INDIVIDUAL, NatureOfControlType.FIRM, NatureOfControlType.UNKNOWN));

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, changesDto, REQUEST_ID, USER_ID))
                .isInstanceOf(MethodArgumentNotValidException.class)
                .hasMessageContaining("Nature of control types must be valid");
    }
}
