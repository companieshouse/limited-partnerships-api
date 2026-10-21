package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;

import java.time.LocalDate;
import java.util.Set;

@Component
public class LimitedPartnershipValidator {
    private static final String CLASS_NAME = DataDto.class.getName();

    private final Validator validator;
    private final CompanyService companyService;

    @Autowired
    public LimitedPartnershipValidator(Validator validator, CompanyService companyService) {
        this.validator = validator;
        this.companyService = companyService;
    }

    public void validatePartial(PartnershipDto partnershipDto, FilingMode filingMode) throws NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(partnershipDto, DataDto.class.getName());

        dtoValidation(partnershipDto, bindingResult);

        if (filingMode != null) {
            checkJourneySpecificFields(partnershipDto.getData(), filingMode, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            MethodParameter methodParameter = new MethodParameter(DataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateUpdate(PartnershipDto partnershipDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        MethodParameter methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(partnershipDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(partnershipDto, bindingResult);

        validateTermAndSicCode(partnershipDto, FilingMode.fromDescription(transaction.getFilingMode()), bindingResult);

        validateDateOfUpdate(transaction, partnershipDto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void validateDateOfUpdate(Transaction transaction, PartnershipDto partnershipDto, BindingResult bindingResult) throws ServiceException {
        if (partnershipDto.getData().getDateOfUpdate() != null) {
            CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(transaction.getCompanyNumber());

            LocalDate dateEffectiveFrom = partnershipDto.getData().getDateOfUpdate();

            if (dateEffectiveFrom.isBefore(companyProfileApi.getDateOfCreation())) {
                addError("data.dateOfUpdate", "Limited partnership date of update cannot be before the incorporation date", bindingResult);
            }
        }
    }

    private void validateTermAndSicCode(PartnershipDto partnershipDto, FilingMode filingMode, BindingResult bindingResult) {
        if (filingMode.equals(FilingMode.TRANSITION) || (PartnershipType.PFLP.equals(partnershipDto.getData().getPartnershipType())
            || PartnershipType.SPFLP.equals(partnershipDto.getData().getPartnershipType()))) {

            if (partnershipDto.getData().getTerm() != null) {
                addError("data.term", "Term is only applicable for non private limited partnerships", bindingResult);
            }

            if ((filingMode.equals(FilingMode.REGISTRATION) || filingMode.equals(FilingMode.TRANSITION)) && partnershipDto.getData().getSicCodes() != null && !partnershipDto.getData().getSicCodes().isEmpty()) {
                addError("data.sicCodes", "SIC codes are only applicable for non private limited partnerships", bindingResult);
            }
        }
    }

    private void checkJourneySpecificFields(DataDto dataDto, FilingMode filingMode, BindingResult bindingResult) {
        if (FilingMode.REGISTRATION.equals(filingMode)) {
            if (dataDto.getNameEnding() == null) {
                addError("data.nameEnding", "Name ending is required", bindingResult);
            }
        } else {
            if (dataDto.getPartnershipNumber() == null) {
                addError("data.partnershipNumber", "Partnership number is required", bindingResult);
            }
        }
    }

    private void dtoValidation(PartnershipDto partnershipDto, BindingResult bindingResult) {
        Set<ConstraintViolation<PartnershipDto>> violations = validator.validate(partnershipDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    addError(violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    private void addError(String fieldName, String defaultMessage, BindingResult bindingResult) {
        bindingResult.addError(new FieldError(CLASS_NAME, fieldName, defaultMessage));
    }
}
