package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;

import java.util.List;

@Component
@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface PscMapper {
    PscMapper INSTANCE = Mappers.getMapper(PscMapper.class);

    PscDto daoToDto(PscDao dao);

    PscDao dtoToDao(PscDto dto);

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

    default String mapNatureOfControlToString(NatureOfControl natureOfControl) {
        return natureOfControl.getDescription();
    }

    default NatureOfControl mapNatureOfControlToEnum(String natureOfControl) {
        return natureOfControl != null ? NatureOfControl.fromDescription(natureOfControl) : null;
    }

    // Java
    default List<String> mapNatureOfControlListToStringList(List<NatureOfControl> natures) {
        return natures == null ? null : natures.stream()
                .map(this::mapNatureOfControlToString)
                .toList();
    }

    default List<NatureOfControl> mapStringListToNatureOfControlList(List<String> natures) {
        return natures == null ? null : natures.stream()
                .map(this::mapNatureOfControlToEnum)
                .toList();
    }

}
