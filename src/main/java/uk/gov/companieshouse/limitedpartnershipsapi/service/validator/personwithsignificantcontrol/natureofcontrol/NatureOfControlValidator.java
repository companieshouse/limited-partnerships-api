package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

@Component
public class NatureOfControlValidator {
	private final NatureOfControlIndividualValidator natureOfControlIndividualValidator;
	private final NatureOfControlFirmValidator natureOfControlFirmValidator;

	NatureOfControlValidator(NatureOfControlIndividualValidator natureOfControlIndividualValidator,
	                         NatureOfControlFirmValidator natureOfControlFirmValidator) {
		this.natureOfControlIndividualValidator = natureOfControlIndividualValidator;
		this.natureOfControlFirmValidator = natureOfControlFirmValidator;
	}

	public boolean isValid(NatureOfControlDto natureOfControlDto) {

		if (natureOfControlDto.getType() == NatureOfControlType.INDIVIDUAL) {
			return natureOfControlIndividualValidator.isValid(natureOfControlDto);
		} else if (natureOfControlDto.getType() == NatureOfControlType.FIRM) {
			return natureOfControlFirmValidator.isValid(natureOfControlDto);
		}

		return false;
	}
}
