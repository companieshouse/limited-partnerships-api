package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceUpdateTest extends AbstractPartnershipServiceTestSupport {
	Transaction transaction = new TransactionBuilder().build();

	private static final String USER_ID = "xbJf0l";
	private static final String REQUEST_ID = "fd4gld5h3jhh";
	private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

	@Autowired
	private PartnershipService partnershipService;

	@MockitoBean
	private PartnershipRepository partnershipRepository;

	@Captor
	private ArgumentCaptor<PartnershipDao> submissionCaptor;

	@Test
	void shouldUpdateTheDao() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
		mockTransactionServicePatchRequest(transaction);

		PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID).buildDao();
		partnershipDao.setUpdatedBy(USER_ID);
		partnershipDao.setTransactionId(transaction.getId());

		PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
		limitedPartnershipPatchDto.setPartnershipName("Test Partnership Updated");

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

		// dao partnership name before mapping/update
		assertThat(partnershipDao.getData().getPartnershipName()).isEqualTo("Test Partnership");

		partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

		verify(partnershipRepository).save(submissionCaptor.capture());

		PartnershipDao sentSubmission = submissionCaptor.getValue();

		assertThat(sentSubmission.getUpdatedBy()).isEqualTo(USER_ID);
		assertThat(sentSubmission.getData().getPartnershipName()).isEqualTo("Test Partnership Updated");

		// copyMetaDataForUpdate
		assertThat(sentSubmission.getId()).isEqualTo(partnershipDao.getId());
		assertThat(sentSubmission.getCreatedAt()).isEqualTo(partnershipDao.getCreatedAt());
		assertThat(sentSubmission.getUpdatedBy()).isEqualTo(partnershipDao.getUpdatedBy());
		assertThat(sentSubmission.getLinks()).isEqualTo(partnershipDao.getLinks());
		assertThat(sentSubmission.getTransactionId()).isEqualTo(transaction.getId());

		// setAuditDetailsForUpdate
		assertThat(sentSubmission.getUpdatedBy()).isEqualTo(USER_ID);
	}

	@Test
	void shouldUpdateTheDaoWithDateOfUpdate() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
		mockTransactionServicePatchRequest(transaction);
		mockCompanyServiceGetRequest();

		PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID).buildDao();

		PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
		limitedPartnershipPatchDto.setDateOfUpdate(LocalDate.of(2026, 6, 1));

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

		partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

		verify(partnershipRepository).save(submissionCaptor.capture());

		PartnershipDao sentSubmission = submissionCaptor.getValue();

		assertThat(sentSubmission.getData().getDateOfUpdate()).isEqualTo(LocalDate.of(2026, 6, 1));
	}

	@Test
	void shouldFailToUpdateTheDaoIfDateOfUpdateBeforeCompanyIncorporation() throws ApiErrorResponseException, URIValidationException {
		mockTransactionServicePatchRequest(transaction);
		mockCompanyServiceGetRequest();

		PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID).buildDao();

		PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
		limitedPartnershipPatchDto.setDateOfUpdate(LocalDate.of(2022, 6, 1));

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

		assertThatThrownBy(() -> partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID))
				.isInstanceOfSatisfying(MethodArgumentNotValidException.class, exception -> {
					assertThat(exception.getBindingResult().getErrorCount()).isEqualTo(1);
					assertThat(exception.getBindingResult().getAllErrors())
							.extracting(DefaultMessageSourceResolvable::getDefaultMessage)
							.containsExactlyInAnyOrder(
									"Limited partnership date of update cannot be before the incorporation date");
				});
	}

	@Nested
	class TermUpdateTests {

		private static final String TERM_NON_PRIVATE_ERROR = "Term is only applicable for non private limited partnerships";
		private static final String TERM_ERROR = "Term is not applicable";

		static Stream<Arguments> termFailureScenarios() {
			return Stream.of(
					Arguments.of(PartnershipType.PFLP, FilingMode.REGISTRATION, TERM_NON_PRIVATE_ERROR),
					Arguments.of(PartnershipType.SPFLP, FilingMode.REGISTRATION, TERM_NON_PRIVATE_ERROR),
					Arguments.of(PartnershipType.PFLP, FilingMode.TRANSITION, TERM_ERROR),
					Arguments.of(PartnershipType.SPFLP, FilingMode.TRANSITION, TERM_ERROR),
					Arguments.of(PartnershipType.LP, FilingMode.TRANSITION, TERM_ERROR),
					Arguments.of(PartnershipType.SLP, FilingMode.TRANSITION, TERM_ERROR),
					Arguments.of(PartnershipType.PFLP, FilingMode.POST_TRANSITION, TERM_NON_PRIVATE_ERROR),
					Arguments.of(PartnershipType.SPFLP, FilingMode.POST_TRANSITION, TERM_NON_PRIVATE_ERROR)
			);
		}

		@ParameterizedTest(name = "{0} in {1} mode should fail with: {2}")
		@MethodSource("termFailureScenarios")
		void shouldFailToUpdateTheTermWhenNotApplicable(PartnershipType partnershipType, FilingMode filingMode, String expectedError) throws ApiErrorResponseException, URIValidationException {
			Transaction testTransaction = new TransactionBuilder().withIncorporationKind(filingMode).build();
			mockTransactionServicePatchRequest(testTransaction);

			PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID)
					.withPartnershipType(partnershipType).withTerm(null).withSicCodes(null).buildDao();

			PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
			limitedPartnershipPatchDto.setTerm(Term.BY_AGREEMENT);

			when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

			assertThatThrownBy(() -> partnershipService.updateLimitedPartnership(testTransaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID))
					.isInstanceOfSatisfying(MethodArgumentNotValidException.class, exception -> {
						assertThat(exception.getBindingResult().getErrorCount()).isEqualTo(1);
						assertThat(exception.getBindingResult().getAllErrors())
								.extracting(DefaultMessageSourceResolvable::getDefaultMessage)
								.containsExactly(expectedError);
					});
		}

		@ParameterizedTest(name = "{0} in {1} mode should be updated")
		@CsvSource({
				"LP, REGISTRATION",
				"SLP, REGISTRATION",
				"LP, POST_TRANSITION",
				"SLP, POST_TRANSITION"
		})
		void shouldUpdateTheTermWhenApplicable(PartnershipType partnershipType, FilingMode filingMode) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
			Transaction testTransaction = new TransactionBuilder().withIncorporationKind(filingMode).build();
			mockTransactionServicePatchRequest(testTransaction);

			PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID)
					.withPartnershipType(partnershipType).withTerm(null).withSicCodes(null).buildDao();

			PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
			limitedPartnershipPatchDto.setTerm(Term.BY_AGREEMENT);

			when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

			partnershipService.updateLimitedPartnership(testTransaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

			verify(partnershipRepository).save(submissionCaptor.capture());

			assertThat(submissionCaptor.getValue().getData().getTerm()).isEqualTo(Term.BY_AGREEMENT);
		}
	}

	@Nested
	class SicCodesUpdateTests {

		private static final String SIC_CODES_NON_PRIVATE_ERROR = "SIC codes are only applicable for non private limited partnerships";
		private static final String SIC_CODES_ERROR = "SIC codes are not applicable";

		static Stream<Arguments> sicCodesFailureScenarios() {
			return Stream.of(
					Arguments.of(PartnershipType.PFLP, FilingMode.REGISTRATION, SIC_CODES_NON_PRIVATE_ERROR),
					Arguments.of(PartnershipType.SPFLP, FilingMode.REGISTRATION, SIC_CODES_NON_PRIVATE_ERROR),
					Arguments.of(PartnershipType.PFLP, FilingMode.TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.SPFLP, FilingMode.TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.LP, FilingMode.TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.SLP, FilingMode.TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.PFLP, FilingMode.POST_TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.SPFLP, FilingMode.POST_TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.LP, FilingMode.POST_TRANSITION, SIC_CODES_ERROR),
					Arguments.of(PartnershipType.SLP, FilingMode.POST_TRANSITION, SIC_CODES_ERROR)
			);
		}

		@ParameterizedTest(name = "{0} in {1} mode should fail with: {2}")
		@MethodSource("sicCodesFailureScenarios")
		void shouldFailToUpdateTheSicCodesWhenNotApplicable(PartnershipType partnershipType, FilingMode filingMode, String expectedError) throws ApiErrorResponseException, URIValidationException {
			Transaction testTransaction = new TransactionBuilder().withIncorporationKind(filingMode).build();
			mockTransactionServicePatchRequest(testTransaction);

			PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID)
					.withPartnershipType(partnershipType).withTerm(null).withSicCodes(null).buildDao();

			PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
			limitedPartnershipPatchDto.setSicCodes(List.of("12345"));

			when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

			assertThatThrownBy(() -> partnershipService.updateLimitedPartnership(testTransaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID))
					.isInstanceOfSatisfying(MethodArgumentNotValidException.class, exception -> {
						assertThat(exception.getBindingResult().getErrorCount()).isEqualTo(1);
						assertThat(exception.getBindingResult().getAllErrors())
								.extracting(DefaultMessageSourceResolvable::getDefaultMessage)
								.containsExactly(expectedError);
					});
		}

		@ParameterizedTest(name = "{0} in {1} mode should be updated")
		@CsvSource({
				"LP, REGISTRATION",
				"SLP, REGISTRATION"
		})
		void shouldUpdateTheSicCodesWhenApplicable(PartnershipType partnershipType, FilingMode filingMode) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
			Transaction testTransaction = new TransactionBuilder().withIncorporationKind(filingMode).build();
			mockTransactionServicePatchRequest(testTransaction);

			PartnershipDao partnershipDao = new PartnershipBuilder().withId(PartnershipBuilder.SUBMISSION_ID)
					.withPartnershipType(partnershipType).withTerm(null).withSicCodes(null).buildDao();

			PartnershipPatchDto limitedPartnershipPatchDto = new PartnershipPatchDto();
			limitedPartnershipPatchDto.setSicCodes(List.of("12345"));

			when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

			partnershipService.updateLimitedPartnership(testTransaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

			verify(partnershipRepository).save(submissionCaptor.capture());

			assertThat(submissionCaptor.getValue().getData().getSicCodes()).containsExactly("12345");
		}
	}

	// Additional tests can be added here to cover other update scenarios, such as invalid input, missing fields, etc.
	// Missing validation have to be added to the service to ensure that the update is valid before saving to the repository, like checking address against jurisdiction, etc.
}
