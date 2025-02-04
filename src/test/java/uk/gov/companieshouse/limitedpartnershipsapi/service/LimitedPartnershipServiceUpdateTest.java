package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnershipServiceUpdateTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String TRANSACTION_ID = "txn-456";
    private static final String LINK_SELF = "self";

    @Autowired
    private LimitedPartnershipService service;

    @MockBean
    private LimitedPartnershipSubmissionsRepository repository;

    @MockBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnershipSubmissionDao> submissionCaptor;

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/txn-456/limited-partnership/partnership/abc-123");
        resource.setLinks(links);

        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        return transaction;
    }

    private LimitedPartnershipSubmissionDao createDao() {
        LimitedPartnershipSubmissionDao dao = new LimitedPartnershipSubmissionDao();
        dao.setId(SUBMISSION_ID);
        DataDao dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Adders");
        dao.setData(dataDao);
        return dao;
    }

    @Nested
    public class updateLimitedPartnership {

        @Nested
        public class updatePartnershipName {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();
                var dataDao = new DataDao();
                dataDao.setPartnershipName("Asset Strippers");
                dataDao.setNameEnding(PartnershipNameEnding.LP.getDescription());
                limitedPartnershipSubmissionDao.setData(dataDao);
                limitedPartnershipSubmissionDao.setCreatedBy("5fd36577288e");

                Transaction transaction = buildTransaction();
                var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setPartnershipName("Asset Strippers Updated");

                when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

                // dao name before mapping/update
                assertEquals("Asset Strippers", limitedPartnershipSubmissionDao.getData().getPartnershipName());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository, times(1)).findById(SUBMISSION_ID);
                verify(repository, times(1)).save(submissionCaptor.capture());

                LimitedPartnershipSubmissionDao sentSubmission = submissionCaptor.getValue();
                assertEquals("5fd36577288e", sentSubmission.getCreatedBy());
                assertEquals(USER_ID, sentSubmission.getUpdatedBy());

                assertEquals("Asset Strippers Updated", sentSubmission.getData().getPartnershipName());
            }

            @Test
            void shouldReturnDtoContainingPartnershipName() throws ResourceNotFoundException {
                // given
                LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();
                limitedPartnershipSubmissionDao.getData().setPartnershipName("Asset Strippers Updated");

                Transaction transaction = buildTransaction();

                when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

                // when
                LimitedPartnershipSubmissionDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository, times(1)).findById(limitedPartnershipSubmissionDao.getId());
                assertEquals("Asset Strippers Updated", retrievedDto.getData().getPartnershipName());
            }
        }

        @Nested
        public class updateRegisteredOfficeAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                Transaction transaction = buildTransaction();

                LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();

                AddressDto registeredOfficeAddress = new AddressDto();
                registeredOfficeAddress.setAddressLine1("DUNCALF STREET");
                registeredOfficeAddress.setCountry("GB-ENG");
                registeredOfficeAddress.setLocality("STOKE-ON-TRENT");
                registeredOfficeAddress.setPostalCode("ST6 3LJ");
                registeredOfficeAddress.setPremises("2");

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

                // dao registered office address is null before mapping/update
                assertNull(limitedPartnershipSubmissionDao.getData().getRegisteredOfficeAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository, times(1)).findById(SUBMISSION_ID);
                verify(repository, times(1)).save(submissionCaptor.capture());

                LimitedPartnershipSubmissionDao sentSubmission = submissionCaptor.getValue();

                assertEquals("DUNCALF STREET", sentSubmission.getData().getRegisteredOfficeAddress().getAddressLine1());
                assertEquals("GB-ENG", sentSubmission.getData().getRegisteredOfficeAddress().getCountry());
                assertEquals("STOKE-ON-TRENT", sentSubmission.getData().getRegisteredOfficeAddress().getLocality());
                assertEquals("ST6 3LJ", sentSubmission.getData().getRegisteredOfficeAddress().getPostalCode());
                assertEquals("2", sentSubmission.getData().getRegisteredOfficeAddress().getPremises());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                Transaction transaction = buildTransaction();

                AddressDao registeredOfficeAddress = new AddressDao();
                registeredOfficeAddress.setAddressLine1("DUNCALF STREET");
                registeredOfficeAddress.setCountry("GB-ENG");
                registeredOfficeAddress.setLocality("STOKE-ON-TRENT");
                registeredOfficeAddress.setPostalCode("ST6 3LJ");
                registeredOfficeAddress.setPremises("2");

                LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();
                limitedPartnershipSubmissionDao.getData().setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));

                // when
                LimitedPartnershipSubmissionDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository, times(1)).findById(limitedPartnershipSubmissionDao.getId());

                assertEquals("DUNCALF STREET", retrievedDto.getData().getRegisteredOfficeAddress().getAddressLine1());
                assertEquals("GB-ENG", retrievedDto.getData().getRegisteredOfficeAddress().getCountry());
                assertEquals("STOKE-ON-TRENT", retrievedDto.getData().getRegisteredOfficeAddress().getLocality());
                assertEquals("ST6 3LJ", retrievedDto.getData().getRegisteredOfficeAddress().getPostalCode());
                assertEquals("2", retrievedDto.getData().getRegisteredOfficeAddress().getPremises());
            }
        }
    }
}
