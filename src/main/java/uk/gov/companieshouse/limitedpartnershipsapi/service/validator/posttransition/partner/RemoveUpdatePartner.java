package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

@Component
public class RemoveUpdatePartner {
    public void validateRemove(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (partnerDto.getData().getCeaseDate() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Cease date is required",
                    "data.ceaseDate"));
        }

        if (!partnerDto.getData().getRemoveConfirmationChecked()) {
            errorsList.add(validationStatus.createValidationStatusError("Remove confirmation checked is required",
                    "data.removeConfirmationChecked"));
        }
    }

    public void validateUpdate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (partnerDto.getData().getDateOfUpdate() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Date of update is required",
                    "data.dateOfUpdate"));
        }

        if (partnerDto.getData().getUpdateUsualResidentialAddressRequired() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Update usual residential address choice is required",
                    "data.isUpdateUsualResidentialAddressRequired"));
        }
    }
}
