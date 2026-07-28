package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dto.NatureOfControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.enums.NatureOfControlType;

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
		} else if (natureOfControlDto.getType() == NatureOfControlType.TRUST) {
			return new NatureOfControlTrust().isValid(natureOfControlDto);
		}

		return false;
	}
}
