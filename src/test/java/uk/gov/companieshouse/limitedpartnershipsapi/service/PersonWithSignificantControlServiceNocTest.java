package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.NatureOfControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.NatureOfControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PersonWithSignificantControlServiceNocTest {
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

    static Stream<NatureOfControlDto> provideNaturesOfControlIndividual() {
        return Stream.of(
            new NatureOfControlBuilder().withShareOfAssets25To50().withVotingRightsDoesNotApply().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRights50To75().build(),
            new NatureOfControlBuilder().withShareOfAssets50To75().withVotingRights75To100().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRightsDoesNotApply().withRightToAppointmentAndRemove().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRightsDoesNotApply().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssets25To50().withVotingRightsDoesNotApply().withRightToAppointmentAndRemove().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRights75To100().withRightToAppointmentAndRemove().build(),
            new NatureOfControlBuilder().withShareOfAssets75To100().withVotingRights50To75().withRightToAppointmentAndRemove().build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideNaturesOfControlIndividual")
    void shouldUpdateTheDaoWithNocIndividual(NatureOfControlDto natureOfControlDto) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlBuilder().withNaturesOfControl(List.of(natureOfControlDto)).individualPersonDto();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        assertNull(personWithSignificantControlDao.getData().getNaturesOfControl());

        personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDto.getData(), REQUEST_ID, USER_ID);

        verify(personWithSignificantControlRepository).findById(PSC_ID);
        verify(personWithSignificantControlRepository).save(pscDaoArgumentCaptor.capture());

        PersonWithSignificantControlDao savedPersonWithSignificantControlDao = pscDaoArgumentCaptor.getValue();

        List<NatureOfControlDao> savedNaturesOfControl = savedPersonWithSignificantControlDao.getData().getNaturesOfControl();

        assertEquals(1, savedNaturesOfControl.size());
        NatureOfControlDao savedNatureOfControl = savedNaturesOfControl.getFirst();

        assertEquals(natureOfControlDto.getShareOfAssets25To50(), savedNatureOfControl.getShareOfAssets25To50());
        assertEquals(natureOfControlDto.getShareOfAssets50To75(), savedNatureOfControl.getShareOfAssets50To75());
        assertEquals(natureOfControlDto.getShareOfAssets75To100(), savedNatureOfControl.getShareOfAssets75To100());
        assertEquals(natureOfControlDto.getShareOfAssetsDoesNotApply(), savedNatureOfControl.getShareOfAssetsDoesNotApply());
        assertEquals(natureOfControlDto.getVotingRights25To50(), savedNatureOfControl.getVotingRights25To50());
        assertEquals(natureOfControlDto.getVotingRights50To75(), savedNatureOfControl.getVotingRights50To75());
        assertEquals(natureOfControlDto.getVotingRights75To100(), savedNatureOfControl.getVotingRights75To100());
        assertEquals(natureOfControlDto.getVotingRightsDoesNotApply(), savedNatureOfControl.getVotingRightsDoesNotApply());
        assertEquals(natureOfControlDto.getRightToAppointmentAndRemove(), savedNatureOfControl.getRightToAppointmentAndRemove());
        assertEquals(natureOfControlDto.getSignificantInfluenceControl(), savedNatureOfControl.getSignificantInfluenceControl());
    }

    static Stream<NatureOfControlDto> provideNaturesOfControlIndividualValidationError() {
        return Stream.of(
            new NatureOfControlBuilder().build(),
            new NatureOfControlBuilder().withShareOfAssets25To50().withVotingRights25To50().withRightToAppointmentAndRemove().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRights50To75().withRightToAppointmentAndRemove().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withRightToAppointmentAndRemove().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withVotingRights75To100().withRightToAppointmentAndRemove().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssets50To75().withRightToAppointmentAndRemove().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssets75To100().withVotingRights75To100().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssets25To50().withVotingRightsDoesNotApply().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRights25To50().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withVotingRightsDoesNotApply().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssets50To75().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().build(),
            new NatureOfControlBuilder().withVotingRightsDoesNotApply().build(),
            new NatureOfControlBuilder().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withRightToAppointmentAndRemove().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withVotingRightsDoesNotApply().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withVotingRightsDoesNotApply().withSignificantInfluenceControl().build(),
            new NatureOfControlBuilder().withShareOfAssetsDoesNotApply().withRightToAppointmentAndRemove().build(),
            new NatureOfControlBuilder().withVotingRightsDoesNotApply().withRightToAppointmentAndRemove().build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideNaturesOfControlIndividualValidationError")
    void shouldNotUpdateTheDaoWithIncorrectNocIndividual(NatureOfControlDto natureOfControlDto) {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

        PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlBuilder().withNaturesOfControl(List.of(natureOfControlDto)).individualPersonDto();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDto.getData(), REQUEST_ID, USER_ID))
            .isInstanceOf(MethodArgumentNotValidException.class)
            .hasMessageContaining("Invalid nature of control combination");
    }
}
