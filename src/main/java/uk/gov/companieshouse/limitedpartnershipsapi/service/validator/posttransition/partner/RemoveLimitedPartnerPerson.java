package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.LimitedPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class RemoveLimitedPartnerPerson implements PostTransitionStrategy<PartnerDto> {

    private final LimitedPartnerValidator limitedPartnerValidator;

    RemoveLimitedPartnerPerson(LimitedPartnerValidator limitedPartnerValidator) {
        this.limitedPartnerValidator = limitedPartnerValidator;
    }

    @Override
    public String getKind() {
        return PartnerKind.REMOVE_LIMITED_PARTNER_PERSON.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        List<ValidationStatusError> errorsListValidator = limitedPartnerValidator.validateRemoveStatus((LimitedPartnerDto) partnerDto, transaction);

        errorsList.addAll(errorsListValidator);

        if (partnerDto.getData().getCeaseDate() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Cease date is required",
                    "data.ceaseDate"));
        }

        if (!partnerDto.getData().getRemoveConfirmationChecked()) {
            errorsList.add(validationStatus.createValidationStatusError("Remove confirmation checked is required",
                    "data.removeConfirmationChecked"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
