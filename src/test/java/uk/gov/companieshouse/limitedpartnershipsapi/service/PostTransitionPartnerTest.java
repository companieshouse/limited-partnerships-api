package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.CompanyBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@SpringBootTest
class PostTransitionPartnerTest {

    @Autowired
    private CostsService costsService;

    @Autowired
    private GeneralPartnerService generalPartnerService;

    @Autowired
    private LimitedPartnershipService limitedPartnershipService;

    @Autowired
    private LimitedPartnerService limitedPartnerService;

    @Autowired
    private ValidationStatus validationStatus;

    @MockitoBean
    private GeneralPartnerRepository generalPartnerRepository;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private CompanyService companyService;

    private final Transaction transactionGeneralPartner = new TransactionBuilder().forPartner(
            FILING_KIND_GENERAL_PARTNER,
            URL_GET_GENERAL_PARTNER,
            GeneralPartnerBuilder.GENERAL_PARTNER_ID
    ).withFilingMode(FilingMode.DEFAULT.getDescription()).build();

    private final GeneralPartnerDao generalPartnerPersonDao = new GeneralPartnerBuilder()
            .personDao();

    private final GeneralPartnerDao generalPartnerLegalEntityDao = new GeneralPartnerBuilder()
            .legalEntityDao();

    private final Transaction transactionLimitedPartner = new TransactionBuilder().forPartner(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            LimitedPartnerBuilder.LIMITED_PARTNER_ID
    ).withFilingMode(FilingMode.DEFAULT.getDescription()).build();

    private final LimitedPartnerDao limitedPartnerPersonDao = new LimitedPartnerBuilder()
            .personDao();

    private final LimitedPartnerDao limitedPartnerLegalEntityDao = new LimitedPartnerBuilder()
            .legalEntityDao();

    @BeforeEach
    void setup() {
        transactionGeneralPartner.setFilingMode(FilingMode.DEFAULT.getDescription());
    }

    @Test
    void shouldReturn200IfNoKindMatching() throws ServiceException {

        mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

        generalPartnerPersonDao.getData().setKind(FILING_KIND_GENERAL_PARTNER);

        var exception = assertThrows(ServiceException.class, () ->
                generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId())
        );

        assertEquals(String.format("No strategy found for kind: %s", FILING_KIND_GENERAL_PARTNER), exception.getMessage());
    }

    @Nested
    class RemoveGeneralPartnerPerson {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            generalPartnerPersonDao.getData().setCeaseDate(LocalDate.of(2025, 1, 1));
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoCeaseDate() throws Exception {

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            generalPartnerPersonDao.getData().setCeaseDate(null);
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Cease date is required"),
                            Map.entry("data.removeConfirmationChecked", "Remove confirmation checked is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfCeaseDateBeforeIncorporationDate() throws Exception {
            generalPartnerPersonDao.getData().setCeaseDate(LocalDate.of(2000, 1, 1));
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Partner cease date cannot be before the incorporation date"),
                            Map.entry("data.ceaseDate", "Partner cease date cannot be before the date of birth")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKindROA() throws Exception {

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = costsService.getPostTransitionGeneralPartnerCost(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertNull(result);
        }
    }

    @Nested
    class RemoveLimitedPartnerPerson {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            limitedPartnerPersonDao.getData().setCeaseDate(LocalDate.of(2025, 1, 1));
            limitedPartnerPersonDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, limitedPartnerPersonDao.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoCeaseDate() throws Exception {

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            limitedPartnerPersonDao.getData().setCeaseDate(null);
            limitedPartnerPersonDao.getData().setRemoveConfirmationChecked(false);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, limitedPartnerPersonDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Cease date is required"),
                            Map.entry("data.removeConfirmationChecked", "Remove confirmation checked is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfCeaseDateBeforeIncorporationDate() throws Exception {
            limitedPartnerPersonDao.getData().setCeaseDate(LocalDate.of(2000, 1, 1));
            limitedPartnerPersonDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, limitedPartnerPersonDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Partner cease date cannot be before the incorporation date"),
                            Map.entry("data.ceaseDate", "Partner cease date cannot be before the date of birth")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKind() throws Exception {

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = costsService.getPostTransitionLimitedPartnerCost(transactionLimitedPartner, limitedPartnerPersonDao.getId());

            assertNull(result);
        }
    }

    @Nested
    class RemoveLimitedPartnerLegalEntity {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            limitedPartnerLegalEntityDao.getData().setCeaseDate(LocalDate.of(2025, 1, 1));
            limitedPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, limitedPartnerLegalEntityDao.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoCeaseDate() throws Exception {

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            limitedPartnerLegalEntityDao.getData().setCeaseDate(null);
            limitedPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(false);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, limitedPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Cease date is required"),
                            Map.entry("data.removeConfirmationChecked", "Remove confirmation checked is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfCeaseDateBeforeIncorporationDate() throws Exception {
            limitedPartnerLegalEntityDao.getData().setCeaseDate(LocalDate.of(2000, 1, 1));
            limitedPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, limitedPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Partner cease date cannot be before the incorporation date")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKind() throws Exception {

            mocks(PartnerKind.REMOVE_LIMITED_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = costsService.getPostTransitionLimitedPartnerCost(transactionLimitedPartner, limitedPartnerLegalEntityDao.getId());

            assertNull(result);
        }
    }

    @Nested
    class RemoveGeneralPartnerLegalEntity {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            generalPartnerLegalEntityDao.getData().setCeaseDate(LocalDate.of(2025, 1, 1));
            generalPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoCeaseDate() throws Exception {

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            generalPartnerLegalEntityDao.getData().setCeaseDate(null);
            generalPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Cease date is required"),
                            Map.entry("data.removeConfirmationChecked", "Remove confirmation checked is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfCeaseDateBeforeIncorporationDate() throws Exception {
            generalPartnerLegalEntityDao.getData().setCeaseDate(LocalDate.of(2000, 1, 1));
            generalPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerLegalEntityDao, limitedPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Partner cease date cannot be before the incorporation date")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKindROA() throws Exception {

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = costsService.getPostTransitionGeneralPartnerCost(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertNull(result);
        }
    }

    @Nested
    class AddPartner {

        @Test
        void shouldReturn200IfNoErrors_AddGeneralPartnerPerson() throws Exception {

            // date could be in the past or present
            generalPartnerPersonDao.getData().setDateEffectiveFrom((LocalDate.now()));

            mocks(PartnerKind.ADD_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, GeneralPartnerBuilder.GENERAL_PARTNER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfPartnerDetailsMissing_AddGeneralPartnerPerson() throws Exception {

            mocks(PartnerKind.ADD_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            generalPartnerPersonDao.getData().setUsualResidentialAddress(null);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, GeneralPartnerBuilder.GENERAL_PARTNER_ID);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("usual_residential_address", "Usual residential address is required")
                    );
        }

        @Test
        void shouldReturn200IfNoErrors_AddGeneralPartnerLegalEntity() throws Exception {

            // date could be in the past or present
            generalPartnerLegalEntityDao.getData().setDateEffectiveFrom((LocalDate.now()));

            mocks(PartnerKind.ADD_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, GeneralPartnerBuilder.GENERAL_PARTNER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfPartnerDetailsMissing_AddGeneralPartnerLegalEntity() throws Exception {

            mocks(PartnerKind.ADD_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            generalPartnerLegalEntityDao.getData().setLegalEntityRegisterName(null);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, GeneralPartnerBuilder.GENERAL_PARTNER_ID);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("legal_entity_register_name", "Legal Entity Register Name is required")
                    );
        }

        @Test
        void shouldReturn200IfNoErrors_AddLimitedPartnerPerson() throws Exception {

            // date could be in the past or present
            limitedPartnerPersonDao.getData().setDateEffectiveFrom((LocalDate.now()));

            mocks(PartnerKind.ADD_LIMITED_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, LimitedPartnerBuilder.LIMITED_PARTNER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfPartnerDetailsMissing_AddLimitedPartnerPerson() throws Exception {

            mocks(PartnerKind.ADD_LIMITED_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            limitedPartnerPersonDao.getData().setDateOfBirth(null);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, LimitedPartnerBuilder.LIMITED_PARTNER_ID);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("date_of_birth", "Date of birth is required")
                    );
        }

        @Test
        void shouldReturn200IfNoErrors_AddLimitedPartnerLegalEntity() throws Exception {

            // date could be in the past or present
            limitedPartnerLegalEntityDao.getData().setDateEffectiveFrom((LocalDate.now()));

            mocks(PartnerKind.ADD_LIMITED_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, LimitedPartnerBuilder.LIMITED_PARTNER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfPartnerDetailsMissing_AddLimitedPartnerLegalEntity() throws Exception {

            mocks(PartnerKind.ADD_LIMITED_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            limitedPartnerLegalEntityDao.getData().setGoverningLaw(null);

            var result = limitedPartnerService.validateLimitedPartner(transactionLimitedPartner, LimitedPartnerBuilder.LIMITED_PARTNER_ID);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("governing_law", "Governing Law is required")
                    );
        }
    }

    // -- Update Partner --

    // -- Update General Partner Person --
    @Nested
    class UpdateGeneralPartnerPerson {
        @ParameterizedTest
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        void shouldReturn200IfNoErrorsAllAddressChoicesAreTrueOrFalse(
                boolean usualResidentialAddressRequired, boolean serviceAddressRequired) throws Exception {
            generalPartnerPersonDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerPersonDao.getData().setUpdateUsualResidentialAddressRequired(usualResidentialAddressRequired);
            generalPartnerPersonDao.getData().setUpdateServiceAddressRequired(serviceAddressRequired);

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoDateOfUpdate() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            generalPartnerPersonDao.getData().setDateOfUpdate(null);
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(false);
            generalPartnerPersonDao.getData().setUpdateUsualResidentialAddressRequired(false);
            generalPartnerPersonDao.getData().setUpdateServiceAddressRequired(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.dateOfUpdate", "Date of update is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoUsualResidentialAddressChoice() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            generalPartnerPersonDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(false);
            generalPartnerPersonDao.getData().setUpdateUsualResidentialAddressRequired(null);
            generalPartnerPersonDao.getData().setUpdateServiceAddressRequired(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.updateUsualResidentialAddressRequired", "Update usual residential address choice is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoServiceAddressChoice() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            generalPartnerPersonDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(false);
            generalPartnerPersonDao.getData().setUpdateUsualResidentialAddressRequired(false);
            generalPartnerPersonDao.getData().setUpdateServiceAddressRequired(null);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.updateServiceAddressRequired", "Update service address choice is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfCeaseDateBeforeIncorporationDate() throws Exception {
            generalPartnerPersonDao.getData().setDateOfUpdate(LocalDate.of(2000, 1, 1));
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(true);
            generalPartnerPersonDao.getData().setUpdateUsualResidentialAddressRequired(false);
            generalPartnerPersonDao.getData().setUpdateServiceAddressRequired(false);

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.dateOfUpdate", "Limited partnership date of update cannot be before the incorporation date")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoData() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            generalPartnerPersonDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerPersonDao.getData().setSurname(null);
            generalPartnerPersonDao.getData().setNationality1(null);
            generalPartnerPersonDao.getData().setUpdateUsualResidentialAddressRequired(false);
            generalPartnerPersonDao.getData().setUpdateServiceAddressRequired(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("surname", "Surname is required"),
                            Map.entry("nationality1", "Nationality1 is required")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKindROA() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao, limitedPartnerPersonDao);

            var result = costsService.getPostTransitionGeneralPartnerCost(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertNull(result);
        }
    }

    // -- Update General Partner Legal Entity
    @Nested
    class UpdateGeneralPartnerLegalEntity {
        @ParameterizedTest
        @CsvSource({"true", "false"})
        void shouldReturn200IfNoErrorsAllAddressChoicesAreTrueOrFalse(boolean principalOfficeAddressRequired) throws Exception {
            generalPartnerLegalEntityDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerLegalEntityDao.getData().setUpdatePrincipalOfficeAddressRequired(principalOfficeAddressRequired);

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(result));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoDateOfUpdate() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            generalPartnerLegalEntityDao.getData().setDateOfUpdate(null);
            generalPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(false);
            generalPartnerLegalEntityDao.getData().setUpdatePrincipalOfficeAddressRequired(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.dateOfUpdate", "Date of update is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoPrincipalOfficeAddressChoice() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            generalPartnerLegalEntityDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(false);
            generalPartnerLegalEntityDao.getData().setUpdatePrincipalOfficeAddressRequired(null);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.updatePrincipalOfficeAddressRequired", "Update principal office address choice is required")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfCeaseDateBeforeIncorporationDate() throws Exception {
            generalPartnerLegalEntityDao.getData().setDateOfUpdate(LocalDate.of(2000, 1, 1));
            generalPartnerLegalEntityDao.getData().setRemoveConfirmationChecked(true);
            generalPartnerLegalEntityDao.getData().setUpdatePrincipalOfficeAddressRequired(false);

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.dateOfUpdate", "Limited partnership date of update cannot be before the incorporation date")
                    );
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoData() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            generalPartnerLegalEntityDao.getData().setDateOfUpdate(LocalDate.now());
            generalPartnerLegalEntityDao.getData().setLegalEntityName(null);
            generalPartnerLegalEntityDao.getData().setGoverningLaw(null);
            generalPartnerLegalEntityDao.getData().setLegalEntityRegisterName(null);
            generalPartnerLegalEntityDao.getData().setLegalEntityRegistrationLocation(null);
            generalPartnerLegalEntityDao.getData().setLegalForm(null);
            generalPartnerLegalEntityDao.getData().setRegisteredCompanyNumber(null);
            generalPartnerLegalEntityDao.getData().setUpdatePrincipalOfficeAddressRequired(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertThat(result).hasSize(6)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("legal_entity_name", "Legal Entity Name is required"),
                            Map.entry("legal_form", "Legal Form is required"),
                            Map.entry("governing_law", "Governing Law is required"),
                            Map.entry("legal_entity_register_name", "Legal Entity Register Name is required"),
                            Map.entry("legal_entity_registration_location", "Legal Entity Registration Location is required"),
                            Map.entry("registered_company_number", "Registered Company Number is required")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKindUpdateGeneralPartnerLegalEntity() throws Exception {

            mocks(PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY, generalPartnerLegalEntityDao, limitedPartnerLegalEntityDao);

            var result = costsService.getPostTransitionGeneralPartnerCost(transactionGeneralPartner, generalPartnerLegalEntityDao.getId());

            assertNull(result);
        }
    }

    void mocks(PartnerKind partnerKind, GeneralPartnerDao generalPartnerDao, LimitedPartnerDao limitedPartnerDao) throws ServiceException {
        generalPartnerDao.getData().setKind(partnerKind.getDescription());
        generalPartnerDao.getData().setDateEffectiveFrom(LocalDate.now());
        limitedPartnerDao.getData().setKind(partnerKind.getDescription());
        limitedPartnerDao.getData().setDateEffectiveFrom(LocalDate.now());

        CompanyProfileApi companyProfile = new CompanyBuilder().build();
        when(companyService.getCompanyProfile(any())).thenReturn(companyProfile);

        when(generalPartnerRepository.findById(GeneralPartnerBuilder.GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));
        when(limitedPartnerRepository.findById(LimitedPartnerBuilder.LIMITED_PARTNER_ID)).thenReturn(Optional.of(limitedPartnerDao));

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);
    }
}
