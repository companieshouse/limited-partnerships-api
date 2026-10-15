package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceValidatorTest {
	Transaction transaction = new TransactionBuilder().build();

	private static final String USER_ID = "xbJf0l";
	private static final String REQUEST_ID = "fd4gld5h3jhh";
	private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

	@Autowired
	private PartnershipService partnershipService;

	@MockitoBean
	private PartnershipRepository partnershipRepository;

	@MockitoBean
	private TransactionService transactionService;

	@Captor
	private ArgumentCaptor<PartnershipDao> submissionCaptor;

	@Test
	void shouldCreatePartnership() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {

		PartnershipDto limitedPartnershipDto = new PartnershipBuilder().buildDto();

		when(partnershipRepository.insert(any(PartnershipDao.class))).thenAnswer(invocation -> invocation.getArgument(0));

		partnershipService.createLimitedPartnership(transaction, limitedPartnershipDto, REQUEST_ID, USER_ID);

		verify(partnershipRepository).save(submissionCaptor.capture());

		PartnershipDao sentSubmission = submissionCaptor.getValue();

		assertThat(sentSubmission.getData())
				.usingRecursiveComparison()
				.isEqualTo(limitedPartnershipDto.getData());
	}

	@Test
	void shouldFailCreatePartnership() {

		PartnershipDto limitedPartnershipDto = new PartnershipBuilder().buildDto();
		DataDto dataDto = limitedPartnershipDto.getData();

		dataDto.setPartnershipName(null);
		dataDto.setNameEnding(null);
		dataDto.setPartnershipType(null);
		dataDto.setPartnershipNumber(null);
		dataDto.setEmail(null);
		dataDto.setJurisdiction(null);
		dataDto.setTerm(null);
		dataDto.setSicCodes(null);
		dataDto.setLawfulPurposeStatementChecked(null);
		dataDto.setRegisteredOfficeAddress(null);
		dataDto.setPrincipalPlaceOfBusinessAddress(null);
		dataDto.setDateOfUpdate(null);

		MethodArgumentNotValidException exception = catchThrowableOfType(
				() -> partnershipService.createLimitedPartnership(transaction, limitedPartnershipDto, REQUEST_ID, USER_ID),
				MethodArgumentNotValidException.class);

		assertThat(exception).isNotNull();
		assertThat(exception.getBindingResult().getErrorCount()).isEqualTo(3);
		assertThat(exception.getBindingResult().getAllErrors())
				.hasSize(exception.getBindingResult().getErrorCount())
				.extracting(DefaultMessageSourceResolvable::getDefaultMessage)
				.containsExactlyInAnyOrder(
						"Limited partnership name must not be null",
						"Name ending is required",
						"Partnership type must not be null");
	}

	@Test
	void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
		PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID).buildDao();

		PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
		limitedPartnershipPatchDto.setPartnershipName("Test Partnership Updated");

		System.out.println("TRANSACTION");
		System.out.println(transaction.getResources().entrySet().stream().findFirst());

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));
		when(transactionService.isTransactionLinkedToResource(transaction, String.format(URL_GET_PARTNERSHIP, transaction.getId(), SUBMISSION_ID), FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(true);

		// dao partnership name before mapping/update
		assertEquals("Test Partnership", partnershipDao.getData().getPartnershipName());

		partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

		verify(partnershipRepository).findById(SUBMISSION_ID);
		verify(partnershipRepository).save(submissionCaptor.capture());

		PartnershipDao sentSubmission = submissionCaptor.getValue();
		assertEquals(USER_ID, sentSubmission.getUpdatedBy());

		assertEquals("Test Partnership Updated", sentSubmission.getData().getPartnershipName());
	}
}
