package uk.gov.companieshouse.limitedpartnershipsapi.generalpartner;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.partner.PartnerValidator;

@Component
public class GeneralPartnerValidator extends PartnerValidator {
    private static final String CLASS_NAME = GeneralPartnerDataDto.class.getName();

    @Autowired
    public GeneralPartnerValidator(Validator validator, CompanyService companyService) {
        super(validator, companyService);
    }

    public void validatePartial(GeneralPartnerDto generalPartnerDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, generalPartnerDto, bindingResult);

        var generalPartnerDataDto = generalPartnerDto.getData();

        if (transaction.getFilingMode().equals(FilingMode.REGISTRATION.getDescription()) ||
                (transaction.getFilingMode().equals(FilingMode.DEFAULT.getDescription()) && PartnerKind.isAddPartnerKind(generalPartnerDataDto.getKind()))
        ) {
            var notDisqualifiedStatementChecked = generalPartnerDataDto.getNotDisqualifiedStatementChecked();
            if (notDisqualifiedStatementChecked == null || Boolean.FALSE.equals(notDisqualifiedStatementChecked)) {
                addError(CLASS_NAME, GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD, "Not Disqualified Statement must be checked", bindingResult);
            }
        }

        if (generalPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(CLASS_NAME, generalPartnerDataDto, bindingResult);
            handleLegalEntityRegisterNameAndNumberOptionality(CLASS_NAME, generalPartnerDataDto, bindingResult);
        } else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(CLASS_NAME, generalPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(CLASS_NAME, generalPartnerDataDto, bindingResult);
        } else {
            addError(CLASS_NAME, "", "Some fields are missing", bindingResult);
        }

        checkNotNullDateEffectiveFrom(CLASS_NAME, generalPartnerDto, transaction, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateRemove(GeneralPartnerDto generalPartnerDto, Transaction transaction) throws ServiceException, NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, generalPartnerDto, bindingResult);

        if (PartnerKind.isPersonKind(generalPartnerDto.getData().getKind())) {
            checkNotNullName(CLASS_NAME, generalPartnerDto.getData(), bindingResult);
        }

        validateCeaseDate(CLASS_NAME, transaction, generalPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateUpdate(GeneralPartnerDto generalPartnerDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(CLASS_NAME, generalPartnerDto, bindingResult);

        if (generalPartnerDto.getData().isLegalEntity() || PartnerKind.isLegalEntityKind(generalPartnerDto.getData().getKind())) {
            checkNotNullLegalEntity(CLASS_NAME, generalPartnerDto.getData(), bindingResult);
            handleLegalEntityRegisterNameAndNumberOptionality(CLASS_NAME, generalPartnerDto.getData(), bindingResult);
        } else {
            checkNotNullName(CLASS_NAME, generalPartnerDto.getData(), bindingResult);
            checkFieldNotNull(CLASS_NAME, generalPartnerDto.getData().getNationality1(), PartnerDataDto.NATIONALITY1_FIELD, NATIONALITY_1_IS_REQUIRED, bindingResult);
            isSecondNationalityDifferent(CLASS_NAME, generalPartnerDto.getData(), bindingResult);
        }

        validateDateEffectiveFrom(CLASS_NAME, transaction, generalPartnerDto, bindingResult);

        validateDateOfUpdate(CLASS_NAME, transaction, generalPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }
}
