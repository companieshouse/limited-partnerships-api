package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol.natureofcontrol;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.NatureOfControlDto;

@Component
public class NatureOfControlIndividualValidator {
	public boolean isValid(NatureOfControlDto natureOfControlDto) {
		if (natureOfControlDto == null) {
			return false;
		}

		if (natureOfControlDto.getNatureOfControlType() == null) {
			return false;
		}

		if (!Boolean.TRUE.equals(natureOfControlDto.getRightToAppointmentAndRemovePersons()) &&
				!Boolean.TRUE.equals(natureOfControlDto.getSigInfluenceControl()) &&
				!hasPartRightToShareSurplusAssetsPercent(natureOfControlDto) &&
				!Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssetsDoesNotApply()) &&
				!hasVotingRightsPercent(natureOfControlDto) &&
				!Boolean.TRUE.equals(natureOfControlDto.getVotingRightsDoesNotApply())) {
			return false;
		}

		return true;
	}

	private boolean hasPartRightToShareSurplusAssetsPercent(NatureOfControlDto natureOfControlDto) {
		return Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssets25To50Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssets50To75Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getPartRightToShareSurplusAssets75To100Percent());
	}

	private boolean hasVotingRightsPercent(NatureOfControlDto natureOfControlDto) {
		return Boolean.TRUE.equals(natureOfControlDto.getVotingRights25To50Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRights50To75Percent()) ||
				Boolean.TRUE.equals(natureOfControlDto.getVotingRights75To100Percent());
	}
}
