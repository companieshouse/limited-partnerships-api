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
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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


    @Test
    void shouldUpdateTheDaoWithNoc() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

        NatureOfControlDto natureOfControlDto = new NatureOfControlDto();
        natureOfControlDto.setPartRightToShareSurplusAssets25To50Percent(true);
        natureOfControlDto.setVotingRightsDoesNotApply(true);
        natureOfControlDto.setRightToAppointmentAndRemovePersons(false);
        natureOfControlDto.setSigInfluenceControl(false);
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

        assertTrue(savedNatureOfControl.getPartRightToShareSurplusAssets25To50Percent());
        assertNull(savedNatureOfControl.getPartRightToShareSurplusAssetsDoesNotApply());
        assertTrue(savedNatureOfControl.getVotingRightsDoesNotApply());
        assertFalse(savedNatureOfControl.getRightToAppointmentAndRemovePersons());
        assertFalse(savedNatureOfControl.getSigInfluenceControl());
    }
}
