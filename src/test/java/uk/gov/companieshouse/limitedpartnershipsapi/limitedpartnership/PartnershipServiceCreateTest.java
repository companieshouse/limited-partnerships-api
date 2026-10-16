package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnershipKind;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_COSTS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_VALIDATION_STATUS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceCreateTest extends AbstractPartnershipServiceTestSupport {
	Transaction transaction = new TransactionBuilder().build();

	private static final String USER_ID = "xbJf0l";
	private static final String REQUEST_ID = "fd4gld5h3jhh";

	@Autowired
	private PartnershipService partnershipService;

	@MockitoBean
	private PartnershipRepository partnershipRepository;

	@Captor
	private ArgumentCaptor<PartnershipDao> submissionCaptor;

	@Test
	void shouldCreatePartnership() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
		transaction.setResources(null);

		mockTransactionServicePatchRequest(transaction);

		when(partnershipRepository.insert(any(PartnershipDao.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PartnershipDto limitedPartnershipDto = new PartnershipBuilder().buildDto();

		partnershipService.createLimitedPartnership(transaction, limitedPartnershipDto, REQUEST_ID, USER_ID);

		verify(partnershipRepository).save(submissionCaptor.capture());

		PartnershipDao sentSubmission = submissionCaptor.getValue();

		assertThat(sentSubmission.getData())
				.usingRecursiveComparison()
				.isEqualTo(limitedPartnershipDto.getData());

		assertThat(sentSubmission.getCreatedBy()).isEqualTo(USER_ID);
		assertThat(sentSubmission.getTransactionId()).isEqualTo(transaction.getId());
		assertThat(sentSubmission.getLinks()).isEqualTo(Collections.singletonMap(LINK_SELF, String.format(URL_GET_PARTNERSHIP, transaction.getId(), sentSubmission.getId())));
	}

	@Test
	void shouldFailCreatePartnershipIfTransactionHasExistingPartnership() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
		mockTransactionServicePatchRequest(transaction);

		when(partnershipRepository.insert(any(PartnershipDao.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PartnershipDto limitedPartnershipDto = new PartnershipBuilder().buildDto();

		assertThatThrownBy(() -> partnershipService.createLimitedPartnership(transaction, limitedPartnershipDto, REQUEST_ID, USER_ID))
				.isInstanceOf(ServiceException.class)
				.hasMessage(String.format("The transaction with id %s already has a Limited Partnership associated with it", transaction.getId()));
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

		assertThatThrownBy(() -> partnershipService.createLimitedPartnership(transaction, limitedPartnershipDto, REQUEST_ID, USER_ID))
				.isInstanceOfSatisfying(MethodArgumentNotValidException.class, exception -> {
					assertThat(exception.getBindingResult().getErrorCount()).isEqualTo(3);
					assertThat(exception.getBindingResult().getAllErrors())
							.hasSize(exception.getBindingResult().getErrorCount())
							.extracting(DefaultMessageSourceResolvable::getDefaultMessage)
							.containsExactlyInAnyOrder(
									"Limited partnership name must not be null",
									"Name ending is required",
									"Partnership type must not be null");
				});
	}

	@Nested
	class PostTransition {
		@ParameterizedTest
		@EnumSource(value = PartnershipKind.class, names = {"UNKNOWN"}, mode = EnumSource.Mode.EXCLUDE)
		void shouldCreatePartnershipAndSetExpectedTransactionLinks(PartnershipKind partnershipKind) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, ApiErrorResponseException, URIValidationException {
			Transaction postTransitionTransaction = new TransactionBuilder().withFilingMode(partnershipKind.getDescription()).build();
			postTransitionTransaction.setResources(null);

			mockTransactionServicePatchRequest(postTransitionTransaction);

			postTransitionTransaction.setFilingMode(FilingMode.DEFAULT.getDescription());

			PartnershipDto limitedPartnershipDto = new PartnershipBuilder().buildDto();
			limitedPartnershipDto.getData().setKind(partnershipKind.getDescription());

			when(partnershipRepository.insert(any(PartnershipDao.class))).thenAnswer(invocation -> invocation.getArgument(0));

			partnershipService.createLimitedPartnership(postTransitionTransaction, limitedPartnershipDto, REQUEST_ID, USER_ID);

			verify(partnershipRepository).save(submissionCaptor.capture());

			PartnershipDao sentSubmission = submissionCaptor.getValue();

			assertThat(sentSubmission.getData())
					.usingRecursiveComparison()
					.isEqualTo(limitedPartnershipDto.getData());

			assertThat(postTransitionTransaction.getResources().values())
					.allSatisfy(resource -> {
						assertThat(resource.getLinks())
								.isNotNull()
								.containsKeys(LINK_RESOURCE, LINK_VALIDATION_STATUS);

						final boolean shouldHaveCostLink = PartnershipKind.UPDATE_PARTNERSHIP_NAME.equals(partnershipKind)
								|| PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP.equals(partnershipKind);

						if (shouldHaveCostLink) {
							assertThat(resource.getLinks())
									.hasSize(3)
									.containsKey(LINK_COSTS);
						} else {
							assertThat(resource.getLinks())
									.hasSize(2)
									.doesNotContainKey(LINK_COSTS);
						}
					});
		}
	}
}
