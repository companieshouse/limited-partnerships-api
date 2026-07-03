package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControlType;

public class NatureOfControlDao {
	@Field("type")
	private NatureOfControlType type;

	@Field("share_of_assets_25_to_50")
	private Boolean shareOfAssets25To50;

	@Field("share_of_assets_50_to_75")
	private Boolean shareOfAssets50To75;

	@Field("share_of_assets_75_to_100")
	private Boolean shareOfAssets75To100;

	@Field("share_of_assets_does_not_apply")
	private Boolean shareOfAssetsDoesNotApply;

	@Field("voting_rights_25_to_50")
	private Boolean votingRights25To50;

	@Field("voting_rights_50_to_75")
	private Boolean votingRights50To75;

	@Field("voting_rights_75_to_100")
	private Boolean votingRights75To100;

	@Field("voting_rights_does_not_apply")
	private Boolean votingRightsDoesNotApply;

	@Field("right_to_appointment_and_remove")
	private Boolean rightToAppointmentAndRemove;

	@Field("significant_influence_control")
	private Boolean significantInfluenceControl;

	public NatureOfControlType getType() {
		return type;
	}

	public void setType(NatureOfControlType type) {
		this.type = type;
	}

	public Boolean getShareOfAssets25To50() {
		return shareOfAssets25To50;
	}

	public void setShareOfAssets25To50(Boolean shareOfAssets25To50) {
		this.shareOfAssets25To50 = shareOfAssets25To50;
	}

	public Boolean getShareOfAssets50To75() {
		return shareOfAssets50To75;
	}

	public void setShareOfAssets50To75(Boolean shareOfAssets50To75) {
		this.shareOfAssets50To75 = shareOfAssets50To75;
	}

	public Boolean getShareOfAssets75To100() {
		return shareOfAssets75To100;
	}

	public void setShareOfAssets75To100(Boolean shareOfAssets75To100) {
		this.shareOfAssets75To100 = shareOfAssets75To100;
	}

	public Boolean getShareOfAssetsDoesNotApply() {
		return shareOfAssetsDoesNotApply;
	}

	public void setShareOfAssetsDoesNotApply(Boolean shareOfAssetsDoesNotApply) {
		this.shareOfAssetsDoesNotApply = shareOfAssetsDoesNotApply;
	}

	public Boolean getVotingRights25To50() {
		return votingRights25To50;
	}

	public void setVotingRights25To50(Boolean votingRights25To50) {
		this.votingRights25To50 = votingRights25To50;
	}

	public Boolean getVotingRights50To75() {
		return votingRights50To75;
	}

	public void setVotingRights50To75(Boolean votingRights50To75) {
		this.votingRights50To75 = votingRights50To75;
	}

	public Boolean getVotingRights75To100() {
		return votingRights75To100;
	}

	public void setVotingRights75To100(Boolean votingRights75To100) {
		this.votingRights75To100 = votingRights75To100;
	}

	public Boolean getVotingRightsDoesNotApply() {
		return votingRightsDoesNotApply;
	}

	public void setVotingRightsDoesNotApply(Boolean votingRightsDoesNotApply) {
		this.votingRightsDoesNotApply = votingRightsDoesNotApply;
	}

	public Boolean getRightToAppointmentAndRemove() {
		return rightToAppointmentAndRemove;
	}

	public void setRightToAppointmentAndRemove(Boolean rightToAppointmentAndRemove) {
		this.rightToAppointmentAndRemove = rightToAppointmentAndRemove;
	}

	public Boolean getSignificantInfluenceControl() {
		return significantInfluenceControl;
	}

	public void setSignificantInfluenceControl(Boolean significantInfluenceControl) {
		this.significantInfluenceControl = significantInfluenceControl;
	}
}
