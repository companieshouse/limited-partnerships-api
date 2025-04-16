package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class GeneralPartnerServiceUpdateTest {
    private static final String USER_ID = "xbJf0l";
    private static final String GENERAL_PARTNER_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Autowired
    private GeneralPartnerService service;

    @MockitoBean
    private GeneralPartnerRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

    private GeneralPartnerDao createGeneralPartnerPersonDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setForename("John");
        dataDao.setSurname("Doe");
        dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
        dataDao.setNationality1("American");
        dataDao.setNotDisqualifiedStatementChecked(true);

        dao.setData(dataDao);
        dao.setId(GENERAL_PARTNER_ID);

        return dao;
    }

    private GeneralPartnerDao createGeneralPartnerLegalEntityDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");
        dataDao.setLegalPersonalityStatementChecked(true);

        dao.setData(dataDao);
        dao.setId(GENERAL_PARTNER_ID);

        return dao;
    }

    @Test
    void shouldUpdateTheDaoWithPrincipalOfficeAddress() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();
        generalPartnerDao.getData().setNationality2(Nationality.GREENLANDIC.getDescription());

        AddressDto principalOfficeAddress = new AddressDto();
        principalOfficeAddress.setAddressLine1("DUNCALF STREET");
        principalOfficeAddress.setCountry("England");
        principalOfficeAddress.setLocality("STOKE-ON-TRENT");
        principalOfficeAddress.setPostalCode("ST6 3LJ");
        principalOfficeAddress.setPremises("2");

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setPrincipalOfficeAddress(principalOfficeAddress);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // dao principal office address is null before mapping/update
        assertNull(generalPartnerDao.getData().getPrincipalOfficeAddress());

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(repository).findById(GENERAL_PARTNER_ID);
        verify(repository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals("John", sentSubmission.getData().getForename());
        assertEquals("DUNCALF STREET", sentSubmission.getData().getPrincipalOfficeAddress().getAddressLine1());
        assertEquals("England", sentSubmission.getData().getPrincipalOfficeAddress().getCountry());
        assertEquals("STOKE-ON-TRENT", sentSubmission.getData().getPrincipalOfficeAddress().getLocality());
        assertEquals("ST6 3LJ", sentSubmission.getData().getPrincipalOfficeAddress().getPostalCode());
        assertEquals("2", sentSubmission.getData().getPrincipalOfficeAddress().getPremises());

        // Ensure that second nationality isn't cleared if only address data is updated
        assertEquals(Nationality.GREENLANDIC.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldFailUpdateIfNationalitiesAreTheSame() {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(Nationality.AMERICAN);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID)
        );

        assertEquals("Second nationality must be different from the first", Objects.requireNonNull(exception.getBindingResult().getFieldError("nationality2")).getDefaultMessage());
    }

    @Test
    void shouldAllowUpdateIfNationalitiesAreDifferent() throws Exception {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(Nationality.NEW_ZEALANDER);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(repository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertEquals(Nationality.NEW_ZEALANDER.getDescription(), sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldClearSecondNationalityIfBeingReset() throws Exception {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setNationality1(Nationality.AMERICAN);
        generalPartnerDataDto.setNationality2(null);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(repository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals(Nationality.AMERICAN.getDescription(), sentSubmission.getData().getNationality1());
        assertNull(sentSubmission.getData().getNationality2());
    }

    @Test
    void shouldUpdateTheDaoWithLegalEntityRegistrationLocation() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        GeneralPartnerDao generalPartnerDao = createGeneralPartnerLegalEntityDao();

        GeneralPartnerDataDto generalPartnerDataDto = new GeneralPartnerDataDto();
        generalPartnerDataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);

        when(repository.findById(generalPartnerDao.getId())).thenReturn(Optional.of(generalPartnerDao));

        // dao principal office address before mapping/update
        assertEquals("United Kingdom", generalPartnerDao.getData().getLegalEntityRegistrationLocation());

        service.updateGeneralPartner(GENERAL_PARTNER_ID, generalPartnerDataDto, REQUEST_ID, USER_ID);

        verify(repository).findById(GENERAL_PARTNER_ID);
        verify(repository).save(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();

        assertEquals("England", sentSubmission.getData().getLegalEntityRegistrationLocation());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void shouldCorrectlyUpdateDisqualificationStatementCheckedValue(Boolean input) throws Exception {
        GeneralPartnerDao currentlySavedPartnerDao = createGeneralPartnerPersonDao();

        GeneralPartnerDataDto partnerDataDtoWithChanges = new GeneralPartnerDataDto();
        partnerDataDtoWithChanges.setNotDisqualifiedStatementChecked(input);

        when(repository.findById(currentlySavedPartnerDao.getId())).thenReturn(Optional.of(currentlySavedPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, partnerDataDtoWithChanges, REQUEST_ID, USER_ID);

        verify(repository).findById(GENERAL_PARTNER_ID);
        verify(repository).save(submissionCaptor.capture());

        GeneralPartnerDao newlySavedPartnerDao = submissionCaptor.getValue();

        assertEquals(input == null || input, newlySavedPartnerDao.getData().getNotDisqualifiedStatementChecked());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void shouldCorrectlyUpdateLegalPersonalityStatementCheckedValue(Boolean input) throws Exception {
        GeneralPartnerDao currentlySavedPartnerDao = createGeneralPartnerLegalEntityDao();

        GeneralPartnerDataDto partnerDataDtoWithChanges = new GeneralPartnerDataDto();
        partnerDataDtoWithChanges.setLegalPersonalityStatementChecked(input);

        when(repository.findById(currentlySavedPartnerDao.getId())).thenReturn(Optional.of(currentlySavedPartnerDao));

        service.updateGeneralPartner(GENERAL_PARTNER_ID, partnerDataDtoWithChanges, REQUEST_ID, USER_ID);

        verify(repository).findById(GENERAL_PARTNER_ID);
        verify(repository).save(submissionCaptor.capture());

        GeneralPartnerDao newlySavedPartnerDao = submissionCaptor.getValue();

        assertEquals(input == null || input, newlySavedPartnerDao.getData().getLegalPersonalityStatementChecked());
    }
}
