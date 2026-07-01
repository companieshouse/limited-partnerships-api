package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;

public class NatureOfControlDto {
	@JsonProperty("nature_of_control_type")
	private NatureOfControlType natureOfControlType;

	@JsonProperty("part_righttosharesurplusassets_25to50percent")
	private Boolean partRightToShareSurplusAssets25To50Percent;

	@JsonProperty("part_righttosharesurplusassets_50to75percent")
	private Boolean partRightToShareSurplusAssets50To75Percent;

	@JsonProperty("part_righttosharesurplusassets_75to100percent")
	private Boolean partRightToShareSurplusAssets75To100Percent;

	@JsonProperty("part_righttosharesurplusassets_does_not_apply")
	private Boolean partRightToShareSurplusAssetsDoesNotApply;

	@JsonProperty("votingrights_25to50percent")
	private Boolean votingRights25To50Percent;

	@JsonProperty("votingrights_50to75percent")
	private Boolean votingRights50To75Percent;

	@JsonProperty("votingrights_75to100percent")
	private Boolean votingRights75To100Percent;

	@JsonProperty("votingrights_does_not_apply")
	private Boolean votingRightsDoesNotApply;

	@JsonProperty("righttoappointmentandremovepersons")
	private Boolean rightToAppointmentAndRemovePersons;

	@JsonProperty("siginfluencecontrol")
	private Boolean sigInfluenceControl;

	public NatureOfControlType getNatureOfControlType() {
		return natureOfControlType;
	}

	public void setNatureOfControlType(NatureOfControlType natureOfControlType) {
		this.natureOfControlType = natureOfControlType;
	}

	public Boolean getPartRightToShareSurplusAssets25To50Percent() {
		return partRightToShareSurplusAssets25To50Percent;
	}

	public void setPartRightToShareSurplusAssets25To50Percent(Boolean partRightToShareSurplusAssets25To50Percent) {
		this.partRightToShareSurplusAssets25To50Percent = partRightToShareSurplusAssets25To50Percent;
	}

	public Boolean getPartRightToShareSurplusAssets50To75Percent() {
		return partRightToShareSurplusAssets50To75Percent;
	}

	public void setPartRightToShareSurplusAssets50To75Percent(Boolean partRightToShareSurplusAssets50To75Percent) {
		this.partRightToShareSurplusAssets50To75Percent = partRightToShareSurplusAssets50To75Percent;
	}

	public Boolean getPartRightToShareSurplusAssets75To100Percent() {
		return partRightToShareSurplusAssets75To100Percent;
	}

	public void setPartRightToShareSurplusAssets75To100Percent(Boolean partRightToShareSurplusAssets75To100Percent) {
		this.partRightToShareSurplusAssets75To100Percent = partRightToShareSurplusAssets75To100Percent;
	}

	public Boolean getPartRightToShareSurplusAssetsDoesNotApply() {
		return partRightToShareSurplusAssetsDoesNotApply;
	}

	public void setPartRightToShareSurplusAssetsDoesNotApply(Boolean partRightToShareSurplusAssetsDoesNotApply) {
		this.partRightToShareSurplusAssetsDoesNotApply = partRightToShareSurplusAssetsDoesNotApply;
	}

	public Boolean getVotingRights25To50Percent() {
		return votingRights25To50Percent;
	}

	public void setVotingRights25To50Percent(Boolean votingRights25To50Percent) {
		this.votingRights25To50Percent = votingRights25To50Percent;
	}

	public Boolean getVotingRights50To75Percent() {
		return votingRights50To75Percent;
	}

	public void setVotingRights50To75Percent(Boolean votingRights50To75Percent) {
		this.votingRights50To75Percent = votingRights50To75Percent;
	}

	public Boolean getVotingRights75To100Percent() {
		return votingRights75To100Percent;
	}

	public void setVotingRights75To100Percent(Boolean votingRights75To100Percent) {
		this.votingRights75To100Percent = votingRights75To100Percent;
	}

	public Boolean getVotingRightsDoesNotApply() {
		return votingRightsDoesNotApply;
	}

	public void setVotingRightsDoesNotApply(Boolean votingRightsDoesNotApply) {
		this.votingRightsDoesNotApply = votingRightsDoesNotApply;
	}

	public Boolean getRightToAppointmentAndRemovePersons() {
		return rightToAppointmentAndRemovePersons;
	}

	public void setRightToAppointmentAndRemovePersons(Boolean rightToAppointmentAndRemovePersons) {
		this.rightToAppointmentAndRemovePersons = rightToAppointmentAndRemovePersons;
	}

	public Boolean getSigInfluenceControl() {
		return sigInfluenceControl;
	}

	public void setSigInfluenceControl(Boolean sigInfluenceControl) {
		this.sigInfluenceControl = sigInfluenceControl;
	}
}
