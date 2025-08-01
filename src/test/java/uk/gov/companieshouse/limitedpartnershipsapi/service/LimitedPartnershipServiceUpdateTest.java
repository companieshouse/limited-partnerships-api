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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class LimitedPartnershipServiceUpdateTest {

    Transaction transaction = new TransactionBuilder().build();

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = LimitedPartnershipBuilder.SUBMISSION_ID;
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Autowired
    private LimitedPartnershipService service;

    @MockitoBean
    private LimitedPartnershipRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnershipDao> submissionCaptor;

    private LimitedPartnershipDao createDao() {
        LimitedPartnershipDao dao = new LimitedPartnershipDao();
        dao.setId(SUBMISSION_ID);
        DataDao dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Adders");
        dao.setData(dataDao);
        return dao;
    }

    @Nested
    public class updateLimitedPartnership {

        @Nested
        class updatePartnershipName {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();
                var dataDao = new DataDao();
                dataDao.setPartnershipName("Asset Strippers");
                dataDao.setNameEnding(PartnershipNameEnding.LP.getDescription());
                limitedPartnershipDao.setData(dataDao);
                limitedPartnershipDao.setCreatedBy("5fd36577288e");

                var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setPartnershipName("Asset Strippers Updated");

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao partnership name before mapping/update
                assertEquals("Asset Strippers", limitedPartnershipDao.getData().getPartnershipName());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();
                assertEquals("5fd36577288e", sentSubmission.getCreatedBy());
                assertEquals(USER_ID, sentSubmission.getUpdatedBy());

                assertEquals("Asset Strippers Updated", sentSubmission.getData().getPartnershipName());
            }

            @Test
            void shouldReturnDtoContainingPartnershipName() throws ResourceNotFoundException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();
                limitedPartnershipDao.getData().setPartnershipName("Asset Strippers Updated");

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());
                assertEquals("Asset Strippers Updated", retrievedDto.getData().getPartnershipName());
            }
        }

        @Nested
        class updateRegisteredOfficeAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();

                AddressDto registeredOfficeAddress = getAddressDto();

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao registered office address is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getRegisteredOfficeAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals("DUNCALF STREET", sentSubmission.getData().getRegisteredOfficeAddress().getAddressLine1());
                assertEquals("England", sentSubmission.getData().getRegisteredOfficeAddress().getCountry());
                assertEquals("STOKE-ON-TRENT", sentSubmission.getData().getRegisteredOfficeAddress().getLocality());
                assertEquals("ST6 3LJ", sentSubmission.getData().getRegisteredOfficeAddress().getPostalCode());
                assertEquals("2", sentSubmission.getData().getRegisteredOfficeAddress().getPremises());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                AddressDao registeredOfficeAddress = getAddressDao();

                LimitedPartnershipDao limitedPartnershipDao = createDao();
                limitedPartnershipDao.getData().setRegisteredOfficeAddress(registeredOfficeAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals("DUNCALF STREET", retrievedDto.getData().getRegisteredOfficeAddress().getAddressLine1());
                assertEquals("England", retrievedDto.getData().getRegisteredOfficeAddress().getCountry());
                assertEquals("STOKE-ON-TRENT", retrievedDto.getData().getRegisteredOfficeAddress().getLocality());
                assertEquals("ST6 3LJ", retrievedDto.getData().getRegisteredOfficeAddress().getPostalCode());
                assertEquals("2", retrievedDto.getData().getRegisteredOfficeAddress().getPremises());
            }
        }

        @Nested
        class UpdateTerm {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setTerm(Term.BY_AGREEMENT);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao term is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getTerm());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(Term.BY_AGREEMENT, sentSubmission.getData().getTerm());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();
                limitedPartnershipDao.getData().setTerm(Term.BY_AGREEMENT);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals(Term.BY_AGREEMENT, retrievedDto.getData().getTerm());

            }
        }

        @Nested
        class updatePrincipalPlaceOfBusinessAddress {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();

                AddressDto principalPlaceOfBusinessAddress = getAddressDto();

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setPrincipalPlaceOfBusinessAddress(principalPlaceOfBusinessAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));

                // dao principal place of business address is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getPrincipalPlaceOfBusinessAddress());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals("DUNCALF STREET", sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getAddressLine1());
                assertEquals("England", sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getCountry());
                assertEquals("STOKE-ON-TRENT", sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getLocality());
                assertEquals("ST6 3LJ", sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getPostalCode());
                assertEquals("2", sentSubmission.getData().getPrincipalPlaceOfBusinessAddress().getPremises());
            }

            @Test
            void shouldReturnDtoContainingRegisteredOfficeAddress() throws ResourceNotFoundException {
                // given
                AddressDao principalPlaceOfBusinessAddress = getAddressDao();

                LimitedPartnershipDao limitedPartnershipDao = createDao();
                limitedPartnershipDao.getData().setPrincipalPlaceOfBusinessAddress(principalPlaceOfBusinessAddress);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                        limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals("DUNCALF STREET", retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getAddressLine1());
                assertEquals("England", retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getCountry());
                assertEquals("STOKE-ON-TRENT", retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getLocality());
                assertEquals("ST6 3LJ", retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getPostalCode());
                assertEquals("2", retrievedDto.getData().getPrincipalPlaceOfBusinessAddress().getPremises());
            }
        }

        @Nested
        class updateSicCode {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();

                List<String> sicCodes = Arrays.asList("12A45", "22345", "33345");

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setSicCodes(sicCodes);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // dao sic codes is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getSicCodes());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertEquals(sicCodes, sentSubmission.getData().getSicCodes());
            }

            @Test
            void shouldReturnDtoContainingSicCodes() throws ResourceNotFoundException {
                // given
                List<String> sicCodes = Arrays.asList("12345", "22345", "33345");

                LimitedPartnershipDao limitedPartnershipDao = createDao();
                limitedPartnershipDao.getData().setSicCodes(sicCodes);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertEquals(sicCodes, retrievedDto.getData().getSicCodes());
            }
        }

        @Nested
        class UpdateLawfulPurposeStatementChecked {
            @Test
            void shouldUpdateTheDao() throws ServiceException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();

                LimitedPartnershipPatchDto limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
                limitedPartnershipPatchDto.setLawfulPurposeStatementChecked(Boolean.TRUE);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // dao lawful purpose statement check is null before mapping/update
                assertNull(limitedPartnershipDao.getData().getLawfulPurposeStatementChecked());

                // when
                service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

                // then
                verify(repository).findById(SUBMISSION_ID);
                verify(repository).save(submissionCaptor.capture());

                LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();

                assertTrue(sentSubmission.getData().getLawfulPurposeStatementChecked());
            }

            @Test
            void shouldReturnDtoContainingLawfulPurposeStatementChecked() throws ResourceNotFoundException {
                // given
                LimitedPartnershipDao limitedPartnershipDao = createDao();
                limitedPartnershipDao.getData().setLawfulPurposeStatementChecked(true);

                when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(limitedPartnershipDao));
                when(transactionService.isTransactionLinkedToLimitedPartnership(any(), any())).thenReturn(true);

                // when
                LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

                // then
                verify(repository).findById(limitedPartnershipDao.getId());

                assertTrue(retrievedDto.getData().getLawfulPurposeStatementChecked());
            }
        }
    }

    private static AddressDao getAddressDao() {
        AddressDao addressDao = new AddressDao();
        addressDao.setAddressLine1("DUNCALF STREET");
        addressDao.setCountry("England");
        addressDao.setLocality("STOKE-ON-TRENT");
        addressDao.setPostalCode("ST6 3LJ");
        addressDao.setPremises("2");
        return addressDao;
    }

    private static AddressDto getAddressDto() {
        AddressDto addressDto = new AddressDto();
        addressDto.setAddressLine1("DUNCALF STREET");
        addressDto.setCountry("England");
        addressDto.setLocality("STOKE-ON-TRENT");
        addressDto.setPostalCode("ST6 3LJ");
        addressDto.setPremises("2");
        return addressDto;
    }
}
