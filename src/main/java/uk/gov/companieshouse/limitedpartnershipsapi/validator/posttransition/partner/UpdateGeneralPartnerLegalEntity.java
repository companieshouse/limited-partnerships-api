package uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.GeneralPartnerValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class UpdateGeneralPartnerLegalEntity implements PostTransitionStrategy<PartnerDto> {

    private final GeneralPartnerValidationStatus generalPartnerValidationStatus;
    private final RemoveOrUpdatePartner removeOrUpdatePartner;

    public UpdateGeneralPartnerLegalEntity(GeneralPartnerValidationStatus generalPartnerValidationStatus, RemoveOrUpdatePartner removeOrUpdatePartner) {
        this.generalPartnerValidationStatus = generalPartnerValidationStatus;
        this.removeOrUpdatePartner = removeOrUpdatePartner;
    }

    @Override
    public String getKind() {
        return PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        List<ValidationStatusError> errorsListValidator = generalPartnerValidationStatus.validate((GeneralPartnerDto) partnerDto, transaction, true);

        errorsList.addAll(errorsListValidator);

        removeOrUpdatePartner.validateUpdate(partnerDto, errorsList, validationStatus);
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}

