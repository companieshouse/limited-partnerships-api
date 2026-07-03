package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

@Component
public class NatureOfControlValidator {
	private final NatureOfControlIndividualValidator natureOfControlIndividualValidator;

	NatureOfControlValidator(NatureOfControlIndividualValidator natureOfControlIndividualValidator) {
		this.natureOfControlIndividualValidator = natureOfControlIndividualValidator;
	}

	public boolean isValid(NatureOfControlDto natureOfControlDto) {

		if (natureOfControlDto.getType() == NatureOfControlType.INDIVIDUAL) {
			return natureOfControlIndividualValidator.isValid(natureOfControlDto);
		}

		return true;
	}
}
