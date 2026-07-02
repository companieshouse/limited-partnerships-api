package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;
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


    static Stream<Arguments> provideNaturesOfControlIndividual() {
        return Stream.of(
                Arguments.of(
                    true, null, null, null,
                    null, null, null, true,
                    false, false),
                Arguments.of(
                    null, null, null, true,
                    null, null, null, true,
                    false, false),
                Arguments.of(
                    null, true, null, null,
                    true, null, null, null,
                    false,  false),
                Arguments.of(
                    null, null, null, true,
                    null, null, null, true,
                    true, false),
                Arguments.of(
                    null, null, null, true,
                    null, null, null, true,
                    false, true),
                Arguments.of(
                    null, null, true, null,
                    null, null, null, true,
                    true, false),
                Arguments.of(
                    null, null, null, true,
                    null, true, null, null,
                    true, false),
                Arguments.of(
                    null, null, true, null,
                    null, null, true, null,
                    true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNaturesOfControlIndividual")
    void shouldUpdateTheDaoWithNocIndividual(
            Boolean partRightToShareSurplusAssets25To50Percent,
            Boolean partRightToShareSurplusAssets50To75Percent,
            Boolean partRightToShareSurplusAssets75To100Percent,
            Boolean partRightToShareSurplusAssetsDoesNotApply,

            Boolean votingRights25To50Percent,
            Boolean votingRights50To75Percent,
            Boolean votingRights75To100Percent,
            Boolean votingRightsDoesNotApply,

            Boolean rightToAppointmentAndRemovePersons,
            Boolean sigInfluenceControl
    ) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

        NatureOfControlDto natureOfControlDto = new NatureOfControlDto();
        natureOfControlDto.setNatureOfControlType(NatureOfControlType.INDIVIDUAL);

        natureOfControlDto.setPartRightToShareSurplusAssets25To50Percent(partRightToShareSurplusAssets25To50Percent);
        natureOfControlDto.setPartRightToShareSurplusAssets50To75Percent(partRightToShareSurplusAssets50To75Percent);
        natureOfControlDto.setPartRightToShareSurplusAssets75To100Percent(partRightToShareSurplusAssets75To100Percent);
        natureOfControlDto.setPartRightToShareSurplusAssetsDoesNotApply(partRightToShareSurplusAssetsDoesNotApply);
        natureOfControlDto.setVotingRights25To50Percent(votingRights25To50Percent);
        natureOfControlDto.setVotingRights50To75Percent(votingRights50To75Percent);
        natureOfControlDto.setVotingRights75To100Percent(votingRights75To100Percent);
        natureOfControlDto.setVotingRightsDoesNotApply(votingRightsDoesNotApply);
        natureOfControlDto.setRightToAppointmentAndRemovePersons(rightToAppointmentAndRemovePersons);
        natureOfControlDto.setSigInfluenceControl(sigInfluenceControl);
        PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlBuilder().withNaturesOfControl(List.of(natureOfControlDto)).relevantLegalEntityDto();

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

        assertEquals(partRightToShareSurplusAssets25To50Percent, savedNatureOfControl.getPartRightToShareSurplusAssets25To50Percent());
        assertEquals(partRightToShareSurplusAssets50To75Percent, savedNatureOfControl.getPartRightToShareSurplusAssets50To75Percent());
        assertEquals(partRightToShareSurplusAssets75To100Percent, savedNatureOfControl.getPartRightToShareSurplusAssets75To100Percent());
        assertEquals(partRightToShareSurplusAssetsDoesNotApply, savedNatureOfControl.getPartRightToShareSurplusAssetsDoesNotApply());
        assertEquals(votingRights25To50Percent, savedNatureOfControl.getVotingRights25To50Percent());
        assertEquals(votingRights50To75Percent, savedNatureOfControl.getVotingRights50To75Percent());
        assertEquals(votingRights75To100Percent, savedNatureOfControl.getVotingRights75To100Percent());
        assertEquals(votingRightsDoesNotApply, savedNatureOfControl.getVotingRightsDoesNotApply());
        assertEquals(rightToAppointmentAndRemovePersons, savedNatureOfControl.getRightToAppointmentAndRemovePersons());
        assertEquals(sigInfluenceControl, savedNatureOfControl.getSigInfluenceControl());
    }

    static Stream<Arguments> provideNaturesOfControlIndividualValidationError() {
        return Stream.of(
            Arguments.of(
                null, null, null, null,
                null, null, null, null,
                false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNaturesOfControlIndividualValidationError")
    void shouldNotUpdateTheDaoWithIncorrectNocIndividual(
        Boolean partRightToShareSurplusAssets25To50Percent,
        Boolean partRightToShareSurplusAssets50To75Percent,
        Boolean partRightToShareSurplusAssets75To100Percent,
        Boolean partRightToShareSurplusAssetsDoesNotApply,

        Boolean votingRights25To50Percent,
        Boolean votingRights50To75Percent,
        Boolean votingRights75To100Percent,
        Boolean votingRightsDoesNotApply,

        Boolean rightToAppointmentAndRemovePersons,
        Boolean sigInfluenceControl
    ) {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

        NatureOfControlDto natureOfControlDto = new NatureOfControlDto();
        natureOfControlDto.setNatureOfControlType(NatureOfControlType.INDIVIDUAL);

        natureOfControlDto.setPartRightToShareSurplusAssets25To50Percent(partRightToShareSurplusAssets25To50Percent);
        natureOfControlDto.setPartRightToShareSurplusAssets50To75Percent(partRightToShareSurplusAssets50To75Percent);
        natureOfControlDto.setPartRightToShareSurplusAssets75To100Percent(partRightToShareSurplusAssets75To100Percent);
        natureOfControlDto.setPartRightToShareSurplusAssetsDoesNotApply(partRightToShareSurplusAssetsDoesNotApply);
        natureOfControlDto.setVotingRights25To50Percent(votingRights25To50Percent);
        natureOfControlDto.setVotingRights50To75Percent(votingRights50To75Percent);
        natureOfControlDto.setVotingRights75To100Percent(votingRights75To100Percent);
        natureOfControlDto.setVotingRightsDoesNotApply(votingRightsDoesNotApply);
        natureOfControlDto.setRightToAppointmentAndRemovePersons(rightToAppointmentAndRemovePersons);
        natureOfControlDto.setSigInfluenceControl(sigInfluenceControl);
        PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlBuilder().withNaturesOfControl(List.of(natureOfControlDto)).relevantLegalEntityDto();

        when(personWithSignificantControlRepository.findById(personWithSignificantControlDao.getId())).thenReturn(Optional.of(personWithSignificantControlDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> personWithSignificantControlService.updatePersonWithSignificantControl(transaction, PSC_ID, personWithSignificantControlDto.getData(), REQUEST_ID, USER_ID))
            .isInstanceOf(MethodArgumentNotValidException.class)
            .hasMessageContaining("Invalid nature of control combination");
    }
}
