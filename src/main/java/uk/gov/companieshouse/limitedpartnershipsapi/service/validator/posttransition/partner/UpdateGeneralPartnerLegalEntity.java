package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.GeneralPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class UpdateGeneralPartnerLegalEntity implements PostTransitionStrategy<PartnerDto> {

    private final GeneralPartnerValidator generalPartnerValidator;
    private final RemoveOrUpdatePartner removeOrUpdatePartner;

    public UpdateGeneralPartnerLegalEntity(GeneralPartnerValidator generalPartnerValidator, RemoveOrUpdatePartner removeOrUpdatePartner) {
        this.generalPartnerValidator = generalPartnerValidator;
        this.removeOrUpdatePartner = removeOrUpdatePartner;
    }

    @Override
    public String getKind() {
        return PartnerKind.UPDATE_GENERAL_PARTNER_LEGAL_ENTITY.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsListValidator = generalPartnerValidator.validateFull((GeneralPartnerDto) partnerDto, transaction, true);

        errorsList.addAll(errorsListValidator);

        removeOrUpdatePartner.validateUpdate(partnerDto, errorsList, validationStatus);
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}

