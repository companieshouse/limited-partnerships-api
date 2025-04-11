package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.LimitedPartnerType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;

@Component
@Mapper(componentModel = "spring")
public interface LimitedPartnerMapper {

    LimitedPartnerMapper INSTANCE = Mappers.getMapper(LimitedPartnerMapper.class);

    LimitedPartnerDto daoToDto(LimitedPartnerDao dao);

    LimitedPartnerDao dtoToDao(LimitedPartnerDto dto);

    default String mapPartnerTypeToString(LimitedPartnerType partnerType) {
        return partnerType.getDescription();
    }

    // ENUMS
    default String mapNationalityToString(Nationality nationality) {
        return nationality.getDescription();
    }

    default Nationality mapNationalityToEnum(String nationality) {
        return nationality != null ? Nationality.fromDescription(nationality) : null;
    }

    default String mapCountryToString(Country country) {
        return country.getDescription();
    }

    default Country mapCountryToEnum(String country) {
        return country != null ? Country.fromDescription(country) : null;
    }

}
