package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ALL_DOMESTIC_COUNTRIES;

public class AddressUtils {

    private AddressUtils() {
        // Utility class
    }

    public static void setOverseasAddressIndicators(DataDto limitedPartnershipDataDto) {
        if (limitedPartnershipDataDto == null) {
            return;
        }

        setOverseasIndicatorsForAddresses(safeAddressList(
                limitedPartnershipDataDto.getRegisteredOfficeAddress(),
                limitedPartnershipDataDto.getPrincipalPlaceOfBusinessAddress()
        ));
    }

    public static void setOverseasAddressIndicators(GeneralPartnerDataDto generalPartnerDataDto) {
        if (generalPartnerDataDto == null) {
            return;
        }

        setOverseasIndicatorsForAddresses(safeAddressList(
                generalPartnerDataDto.getServiceAddress(),
                generalPartnerDataDto.getPrincipalOfficeAddress(),
                generalPartnerDataDto.getUsualResidentialAddress()
        ));
    }

    public static void setOverseasAddressIndicators(LimitedPartnerDataDto limitedPartnerDataDto) {
        if (limitedPartnerDataDto == null) {
            return;
        }

        setOverseasIndicatorsForAddresses(safeAddressList(
                limitedPartnerDataDto.getPrincipalOfficeAddress(),
                limitedPartnerDataDto.getUsualResidentialAddress()
        ));
    }

    private static List<AddressDto> safeAddressList(AddressDto... addresses) {
        List<AddressDto> result = new ArrayList<>();
        for (AddressDto address : addresses) {
            if (address != null) {
                result.add(address);
            }
        }
        return result;
    }

    private static void setOverseasIndicatorsForAddresses(List<AddressDto> addresses) {
        addresses.forEach(AddressUtils::setOverseasIndicator);
    }

    private static void setOverseasIndicator(AddressDto addressDto) {
        addressDto.setOverseas(!ALL_DOMESTIC_COUNTRIES.contains(addressDto.getCountry()));
    }
}
