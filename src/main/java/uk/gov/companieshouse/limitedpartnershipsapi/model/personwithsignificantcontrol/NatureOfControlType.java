package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum NatureOfControlType {
		INDIVIDUAL,
		FIRM,
		TRUST,

		@JsonEnumDefaultValue
		UNKNOWN;

		@JsonCreator
		public static NatureOfControlType fromString(String enumAsString) {
			for (NatureOfControlType type : NatureOfControlType.values()) {
				if (type.toString().equalsIgnoreCase(enumAsString)) {
					return type;
				}
			}

			return UNKNOWN;
		}

}
