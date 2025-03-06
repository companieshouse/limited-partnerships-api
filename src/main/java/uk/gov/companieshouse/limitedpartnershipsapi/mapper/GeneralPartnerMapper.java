package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;

@Component
@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface GeneralPartnerMapper {
    GeneralPartnerDto daoToDto(GeneralPartnerDao dao);
    GeneralPartnerDao dtoToDao(GeneralPartnerDto dto);

    GeneralPartnerDataDto map(GeneralPartnerDataDto dto);

    @InheritConfiguration
    void update(GeneralPartnerDataDto update, @MappingTarget GeneralPartnerDataDto destination);

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
