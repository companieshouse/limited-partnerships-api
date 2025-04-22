package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class LimitedPartnerServiceUpdateTest {
    private static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Autowired
    private LimitedPartnerService service;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private LimitedPartnerDao createLimitedPartnerPersonDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setForename("John");
        dataDao.setSurname("Doe");
        dataDao.setDateOfBirth(LocalDate.of(1980, 1, 1));
        dataDao.setNationality1("American");

        dao.setData(dataDao);
        dao.setId(LIMITED_PARTNER_ID);

        return dao;
    }

    @Nested
    class DeleteLimitedPartner {
        private static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
        private static final String TRANSACTION_ID = "863851-951242-143528";

        @Test
        void shouldDeleteLimitedPartner() throws ServiceException {
            Transaction transaction = buildTransaction();

            LimitedPartnerDao limitedPartnerDao = createLimitedPartnerPersonDao();

            // transaction before
            assertEquals(1, transaction.getResources().size());

            when(limitedPartnerRepository.findById(LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

            service.deleteLimitedPartner(transaction, LIMITED_PARTNER_ID, REQUEST_ID);

            verify(transactionService).updateTransaction(transactionCaptor.capture(), eq(REQUEST_ID));

            Transaction transactionUpdated = transactionCaptor.getValue();

            assertEquals(0, transactionUpdated.getResources().size());

            // transaction after
            assertEquals(0, transaction.getResources().size());
        }

        @Test
        void shouldThrowServiceExceptionWhenLimitedPartnerNotFound() {
            Transaction transaction = buildTransaction();

            when(limitedPartnerRepository.findById("wrong-id")).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    service.deleteLimitedPartner(transaction, "wrong-id", REQUEST_ID)
            );

            assertEquals("Limited partner with id wrong-id not found", exception.getMessage());
        }

        private Transaction buildTransaction() {
            Transaction transaction = new Transaction();
            transaction.setId(TRANSACTION_ID);

            Resource resource = new Resource();
            resource.setKind(FILING_KIND_LIMITED_PARTNER);
            Map<String, String> links = new HashMap<>();
            links.put("resource", String.format("/transactions/%s/limited-partnership/limited-partner/%s", TRANSACTION_ID, LIMITED_PARTNER_ID));
            resource.setLinks(links);

            Map<String, Resource> resourceMap = new HashMap<>();
            resourceMap.put(String.format("/transactions/%s/limited-partnership/limited-partner/%s", TRANSACTION_ID, LIMITED_PARTNER_ID), resource);
            transaction.setResources(resourceMap);

            return transaction;
        }
    }
}
