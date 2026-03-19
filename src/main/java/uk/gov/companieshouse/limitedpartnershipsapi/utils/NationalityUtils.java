package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.HasNationality;

public class NationalityUtils {

    private NationalityUtils() {
        // Private constructor to prevent instantiation
    }

    public static void handleSecondNationalityOptionality(HasNationality changesDto,
                                                          HasNationality existingDto) {
        // The first 'not null' check here ensures that second nationality isn't wiped if, for example, only address data is being updated
        if (changesDto.getNationality1() != null && changesDto.getNationality2() == null) {
            existingDto.setNationality2(null);
        }
    }
}
