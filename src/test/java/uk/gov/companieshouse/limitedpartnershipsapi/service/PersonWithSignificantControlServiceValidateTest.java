package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.AddressDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDtoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PersonWithSignificantControlServiceValidateTest {

    private static final String PSC_ID = "psc123";
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    private final Transaction transaction = new TransactionBuilder().withKindAndUri(
            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
            URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL,
            PSC_ID
    ).build();

    @Autowired
    private PersonWithSignificantControlService service;

    @MockitoBean
    private PersonWithSignificantControlRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    private PersonWithSignificantControlDao rlePersonWithSignificantControlDao;

    @BeforeEach
    void setUp() {
        transaction.setFilingMode(FilingMode.REGISTRATION.getDescription());

        rlePersonWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder()
                .withId(PSC_ID)
                .withTransactionId(TRANSACTION_ID)
                .withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withLegalEntityName("Test Legal Entity")
                        .withLegalForm("Test Legal Form")
                        .withGoverningLaw("Test Governing Law")
                        .withType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY)
                        .withPrincipalOfficeAddress(new AddressDaoBuilder()
                                .withAddressLine1("123 Test Street")
                                .withAddressLine2("Test District")
                                .withPremises("Test Premises")
                                .withCountry("Test Country")
                                .withLocality("Test Locality")
                                .withRegion("Test Region")
                                .withPostalCode("TE5 7ST")
                                .build()
                        )
                        .build())
                .build();
    }

    @Nested
    class RelevantLegalEntity {

        @Test
        void shouldReturnNoErrorsWhenPSCDataIsValid() throws ServiceException {
            // given
            when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(rlePersonWithSignificantControlDao));

            // when
            List<ValidationStatusError> results = service.validatePersonsWithSignificantControl(transaction);

            // then
            verify(repository).findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);
            assertEquals(0, results.size());
        }

        @Test
        void shouldReturnErrorIfPOANotSupplied() throws ServiceException {
            // given
            rlePersonWithSignificantControlDao.getData().setPrincipalOfficeAddress(null);

            when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(rlePersonWithSignificantControlDao));

            // when
            List<ValidationStatusError> results = service.validatePersonsWithSignificantControl(transaction);

            // then
            verify(repository).findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);
            assertThat(results).hasSize(1);
            assertThat(results.getFirst())
                .usingRecursiveComparison()
                .isEqualTo(new ValidationStatusError("Principal office address is required", "data.principalOfficeAddress", null, null));
        }

        @Test
        void shouldReturnErrorsIfDataIsInvalid() throws ServiceException {
            // given
            rlePersonWithSignificantControlDao.getData().setLegalEntityName("§§§§§§§");
            when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(rlePersonWithSignificantControlDao));

            // when
            List<ValidationStatusError> results = service.validatePersonsWithSignificantControl(transaction);

            // then
            verify(repository).findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);
            assertThat(results).hasSize(1);
            assertThat(results.getFirst())
                    .usingRecursiveComparison()
                    .isEqualTo(new ValidationStatusError("Name " + INVALID_CHARACTERS_MESSAGE, "data.legalEntityName", null, null));
        }
    }

    @Nested
    class UnknownType {
        private PersonWithSignificantControlDto rlePersonWithSignificantControlDto;


        @BeforeEach
        void setup() {
            rlePersonWithSignificantControlDto = new PersonWithSignificantControlDtoBuilder()
                    .withId(PSC_ID)
                    .withData(new PersonWithSignificantControlDtoBuilder.DataBuilder()
                            .withLegalEntityName("Test Legal Entity")
                            .withLegalForm("Test Legal Form")
                            .withGoverningLaw("Test Governing Law")
                            .withType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY)
                            .build())
                    .build();
        }

        @Test
        void shouldReturnErrorOnPartialValidation() {
            rlePersonWithSignificantControlDto.getData().setType(PersonWithSignificantControlType.UNKNOWN);

            MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class, () -> service.createPersonWithSignificantControl(
                    transaction,
                    rlePersonWithSignificantControlDto,
                    "2121232",
                    "24234234"
            ));

            assertThat(exception.getMessage()).contains("Invalid person with significant control type specified");
        }

        @Test
        void shouldReturnErrorOnFullValidation() throws ServiceException {
            rlePersonWithSignificantControlDao.getData().setType(PersonWithSignificantControlType.UNKNOWN);

            when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(rlePersonWithSignificantControlDao));

            List<ValidationStatusError> errors = service.validatePersonsWithSignificantControl(transaction);

            verify(repository).findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);
            assertThat(errors).hasSize(1);
            assertThat(errors.getFirst())
                    .usingRecursiveComparison()
                    .isEqualTo(new ValidationStatusError("Invalid person with significant control type specified", "data.type", null, null));
        }
    }

}
