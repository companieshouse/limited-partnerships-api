package uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.LimitedPartnerValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class UpdateLimitedPartnerPerson implements PostTransitionStrategy<PartnerDto> {

    private final LimitedPartnerValidationStatus limitedPartnerValidationStatus;
    private final RemoveOrUpdatePartner removeOrUpdatePartner;

    public UpdateLimitedPartnerPerson(LimitedPartnerValidationStatus limitedPartnerValidationStatus, RemoveOrUpdatePartner removeOrUpdatePartner) {
        this.limitedPartnerValidationStatus = limitedPartnerValidationStatus;
        this.removeOrUpdatePartner = removeOrUpdatePartner;
    }

    @Override
    public String getKind() {
        return PartnerKind.UPDATE_LIMITED_PARTNER_PERSON.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsListValidator = limitedPartnerValidationStatus.validate((LimitedPartnerDto) partnerDto, transaction, true);

        errorsList.addAll(errorsListValidator);

        removeOrUpdatePartner.validateUpdate(partnerDto, errorsList, validationStatus);
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
