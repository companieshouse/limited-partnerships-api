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
public class UpdateGeneralPartnerPerson implements PostTransitionStrategy<PartnerDto> {

    private final GeneralPartnerValidator generalPartnerValidator;

    public UpdateGeneralPartnerPerson(GeneralPartnerValidator generalPartnerValidator) {
        this.generalPartnerValidator = generalPartnerValidator;
    }

    @Override
    public String getKind() {
        return PartnerKind.UPDATE_GENERAL_PARTNER_PERSON.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsListValidator = generalPartnerValidator.validateFull((GeneralPartnerDto) partnerDto, transaction);

        errorsList.addAll(errorsListValidator);

        if (partnerDto.getData().getDateOfUpdate() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Date of update is required",
                    "data.dateOfUpdate"));
        }

        if (partnerDto.getData().getUpdateUsualResidentialAddressRequired() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Update usual residential address choice is required",
                    "data.updateUsualResidentialAddressRequired"));
        }

        if (partnerDto.getData().getUpdateServiceAddressRequired() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Update service address choice is required",
                    "data.updateServiceAddressRequired"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
