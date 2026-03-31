package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlSubmissionCreatedResponseDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.PersonWithSignificantControlService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder.TRANSACTION_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
class PersonWithSignificantControlControllerTest {
    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String PERSON_WITH_SIGNIFICANT_CONTROL_ID = PersonWithSignificantControlBuilder.ID;

    @InjectMocks
    private PersonWithSignificantControlController personWithSignificantControlController;

    @Mock
    private PersonWithSignificantControlService personWithSignificantControlService;

    private final Transaction transaction = new TransactionBuilder()
        .withKindAndUri(
                FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL,
                PERSON_WITH_SIGNIFICANT_CONTROL_ID
        )
        .build();

    private PersonWithSignificantControlDto personWithSignificantControlDto;

    @BeforeEach
    void init() { personWithSignificantControlDto = new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build(); }

    @Test
    void testGetPersonWithSignificantControlReturnsDto() throws ServiceException {
        PersonWithSignificantControlDto dto = new PersonWithSignificantControlDto();
        when(personWithSignificantControlService.getPersonWithSignificantControl(transaction, PERSON_WITH_SIGNIFICANT_CONTROL_ID))
                .thenReturn(dto);

        var response = personWithSignificantControlController.getPersonWithSignificantControl(transaction, PERSON_WITH_SIGNIFICANT_CONTROL_ID, REQUEST_ID);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPersonWithSignificantControlThrowsResourceNotFoundException() throws ServiceException {
        when(personWithSignificantControlService.getPersonWithSignificantControl(any(Transaction.class), anyString()))
                .thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> personWithSignificantControlController.getPersonWithSignificantControl(
                transaction, PERSON_WITH_SIGNIFICANT_CONTROL_ID, REQUEST_ID));
    }

    @Test
    void testCreatePersonWithSignificantControlReturnsSuccess() throws Exception {
        when(personWithSignificantControlService.createPersonWithSignificantControl(
                eq(transaction),
                any(PersonWithSignificantControlDto.class),
                eq(REQUEST_ID),
                eq(USER_ID)))
                .thenReturn(PERSON_WITH_SIGNIFICANT_CONTROL_ID);

        var response = personWithSignificantControlController.createPersonWithSignificantControl(
                transaction,
                personWithSignificantControlDto,
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        var responseHeaderLocation = Objects.requireNonNull(response.getHeaders().get(HttpHeaders.LOCATION)).getFirst();
        assertEquals(String.format(URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL, TRANSACTION_ID, PERSON_WITH_SIGNIFICANT_CONTROL_ID), responseHeaderLocation);
        PersonWithSignificantControlSubmissionCreatedResponseDto responseBody = response.getBody();
        assert responseBody != null;
        assertEquals(PERSON_WITH_SIGNIFICANT_CONTROL_ID, responseBody.id());

        assertTrue(StringUtils.isBlank(transaction.getResumeJourneyUri()));
    }

    @Test
    void testCreatePersonWithSignificantControlThrowsServiceException() throws ServiceException {
        ServiceException exception = new ServiceException("Test");
        try (MockedStatic<ApiLogger> mockedLogger = Mockito.mockStatic(ApiLogger.class)) {
            doThrow(exception).when(personWithSignificantControlService).createPersonWithSignificantControl(
                    eq(transaction),
                    any(PersonWithSignificantControlDto.class),
                    eq(REQUEST_ID),
                    eq(USER_ID));

            var response = personWithSignificantControlController.createPersonWithSignificantControl(
                    transaction,
                    personWithSignificantControlDto,
                    REQUEST_ID,
                    USER_ID);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());

            mockedLogger.verify(() -> ApiLogger.errorContext(
                    eq(REQUEST_ID),
                    eq("Error creating the person with significant control"),
                    eq(exception),
                    any()
            ));
        }
    }

    @Test
    void testUpdatePersonWithSignificantControlReturnsSuccess() throws Exception {
        var response = personWithSignificantControlController.updatePersonWithSignificantControl(
                transaction,
                PERSON_WITH_SIGNIFICANT_CONTROL_ID,
                new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build().getData(),
                REQUEST_ID,
                USER_ID);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    }

    @Test
    void testUpdatePersonWithSignificantControlThrowsResourceNotFoundException() throws ServiceException {
        doThrow(ResourceNotFoundException.class).when(personWithSignificantControlService).updatePersonWithSignificantControl(any(Transaction.class), anyString(), any(PersonWithSignificantControlDataDto.class), anyString(), anyString() );

        assertThrows(ResourceNotFoundException.class, () -> personWithSignificantControlController.updatePersonWithSignificantControl(
                transaction,
                PERSON_WITH_SIGNIFICANT_CONTROL_ID,
                new PersonWithSignificantControlBuilder.PersonWithSignificantControlDtoBuilder().personWithSignificantControlDto().build().getData(),
                REQUEST_ID,
                USER_ID));
    }

    @Test
    void testDeletePersonWithSignificantControlReturnsSuccess() throws Exception {
        var response = personWithSignificantControlController.deletePersonWithSignificantControl(
                transaction,
                PERSON_WITH_SIGNIFICANT_CONTROL_ID,
                REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode().value());
        verify(personWithSignificantControlService).deletePersonWithSignificantControl(transaction, PERSON_WITH_SIGNIFICANT_CONTROL_ID, REQUEST_ID);
    }
}
