package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class GeneralPartnerServiceCreateTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String TRANSACTION_ID = "txn-456";

    @Autowired
    private GeneralPartnerService service;

    @MockBean
    private GeneralPartnerRepository repository;

    @MockBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<GeneralPartnerDao> submissionCaptor;

    @Test
    void shouldCreateAGeneralPartnerLegalEntity() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        Transaction transaction = buildTransaction();
        GeneralPartnerDto dto = createGeneralPartnerLegalEntityDto();
        GeneralPartnerDao dao = createGeneralPartnerLegalEntityDao();

        when(repository.insert((GeneralPartnerDao) any())).thenReturn(dao);
        when(repository.save(dao)).thenReturn(dao);

        String submissionId = service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID);

        verify(repository).insert(submissionCaptor.capture());

        GeneralPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_GENERAL_PARTNER, sentSubmission.getData().getKind());
        assertEquals(SUBMISSION_ID, submissionId);

        String expectedUri = String.format(URL_GET_GENERAL_PARTNER, transaction.getId(), SUBMISSION_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void shouldFailCreateAGeneralPartnerLegalEntityIfLegalEntityRegisterNameIsCorrectAndOthersAreNull() throws ServiceException {
        Transaction transaction = buildTransaction();
        GeneralPartnerDto dto = createGeneralPartnerLegalEntityDto();
        dto.getData().setLegalForm(null);
        dto.getData().setGoverningLaw(null);
        dto.getData().setLegalEntityRegistrationLocation(null);
        dto.getData().setCountry(null);
        dto.getData().setRegisteredCompanyNumber(null);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
        );

        assertNull(exception.getBindingResult().getFieldError("legal_entity_register_name"));
        assertEquals("Legal Form must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_form")).getDefaultMessage());
        assertEquals("Governing Law must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
        assertEquals("Legal Entity Registration Location must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
        assertEquals("Country must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("country")).getDefaultMessage());
        assertEquals("Registered Company Number must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
    }

    @Test
    void shouldFailCreateAGeneralPartnerLegalEntityIfLegalFormIsCorrectAndOthersAreNull() throws ServiceException {
        Transaction transaction = buildTransaction();
        GeneralPartnerDto dto = createGeneralPartnerLegalEntityDto();
        dto.getData().setLegalEntityRegisterName(null);
        dto.getData().setGoverningLaw(null);
        dto.getData().setLegalEntityRegistrationLocation(null);
        dto.getData().setCountry(null);
        dto.getData().setRegisteredCompanyNumber(null);

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () ->
                service.createGeneralPartner(transaction, dto, REQUEST_ID, USER_ID)
        );

        assertNull(exception.getBindingResult().getFieldError("legal_form"));
        assertEquals("Legal Entity Register Name must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_register_name")).getDefaultMessage());
        assertEquals("Governing Law must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("governing_law")).getDefaultMessage());
        assertEquals("Legal Entity Registration Location must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("legal_entity_registration_location")).getDefaultMessage());
        assertEquals("Country must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("country")).getDefaultMessage());
        assertEquals("Registered Company Number must not be null", Objects.requireNonNull(exception.getBindingResult().getFieldError("registered_company_number")).getDefaultMessage());
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_GENERAL_PARTNER);
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/txn-456/limited-partnership/general-partner/abc-123");
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    private GeneralPartnerDto createGeneralPartnerLegalEntityDto() {
        GeneralPartnerDto dto = new GeneralPartnerDto();

        GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
        dataDto.setLegalEntityRegisterName("General Partner Legal Entity");
        dataDto.setLegalForm("Limited Company");
        dataDto.setGoverningLaw("Act of law");
        dataDto.setLegalEntityRegistrationLocation("Public Register");
        dataDto.setCountry("United Kingdom");
        dataDto.setRegisteredCompanyNumber("12345678");
        dataDto.setNotDisqualifiedStatementChecked(true);

        dto.setData(dataDto);
        return dto;
    }

    private GeneralPartnerDao createGeneralPartnerLegalEntityDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityRegisterName("General Partner Legal Entity");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegistrationLocation("Public Register");
        dataDao.setCountry("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");
        dataDao.setNotDisqualifiedStatementChecked(true);

        dao.setData(dataDao);
        dao.setId(SUBMISSION_ID);

        return dao;
    }

}
