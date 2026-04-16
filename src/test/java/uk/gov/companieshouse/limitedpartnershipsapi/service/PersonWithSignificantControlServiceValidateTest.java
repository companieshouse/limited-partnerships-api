package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PersonWithSignificantControlRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PERSON_WITH_SIGNIFICANT_CONTROL;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PersonWithSignificantControlServiceValidateTest {

    private static final String PSC_ID = PersonWithSignificantControlBuilder.ID;
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

    @BeforeEach
    void setUp() {
        transaction.setFilingMode(FilingMode.REGISTRATION.getDescription());
    }

    @Nested
    class RelevantLegalEntity {

        @Test
        void shouldReturnNoErrorsWhenPSCDataIsValid() throws ServiceException {
            // given
            PersonWithSignificantControlDao personWithSignificantControlDao =
                    new PersonWithSignificantControlBuilder
                            .PersonWithSignificantControlDaoBuilder()
                            .legalEntityPersonWithSignificantControlDao()
                            .build();
            personWithSignificantControlDao.getData().setType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY);
            personWithSignificantControlDao.setTransactionId(TRANSACTION_ID);

            when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(personWithSignificantControlDao));

            // when
            List<ValidationStatusError> results = service.validatePersonsWithSignificantControl(transaction);

            // then
            verify(repository).findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);
            assertEquals(0, results.size());
        }

        @Test
        void shouldReturnErrorIfPOANotSupplied() throws ServiceException {
            // given
            PersonWithSignificantControlDao personWithSignificantControlDao =
                    new PersonWithSignificantControlBuilder
                            .PersonWithSignificantControlDaoBuilder()
                            .legalEntityPersonWithSignificantControlDao()
                            .withPrincipalOfficeAddress(null)
                            .build();
            personWithSignificantControlDao.getData().setType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY);
            personWithSignificantControlDao.setTransactionId(TRANSACTION_ID);

            when(repository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(List.of(personWithSignificantControlDao));

            // when
            List<ValidationStatusError> results = service.validatePersonsWithSignificantControl(transaction);

            // then
            verify(repository).findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID);
            assertThat(results).hasSize(1);
            assertThat(results.getFirst())
                .usingRecursiveComparison()
                .isEqualTo(new ValidationStatusError("Principal office address is required", "data.principalOfficeAddress", null, null));
        }

    }

}
