package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import com.google.common.collect.Lists;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;

import java.util.List;

@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface PersonWithSignificantControlMapper {
    PersonWithSignificantControlDto daoToDto(PersonWithSignificantControlDao dao);

    PersonWithSignificantControlDao dtoToDao(PersonWithSignificantControlDto dto);

    PersonWithSignificantControlDataDto map(PersonWithSignificantControlDataDto dto);

    @InheritConfiguration
    void update(PersonWithSignificantControlDataDto update, @MappingTarget PersonWithSignificantControlDataDto destination);

    // ENUMS
    default String mapNationalityToString(Nationality nationality) {
        return nationality != null ? nationality.getDescription() : null;
    }

    default Nationality mapNationalityToEnum(String nationality) {
        return nationality != null ? Nationality.fromDescription(nationality) : null;
    }

    default String mapCountryToString(Country country) {

        return country != null ? country.getDescription() : null;
    }

    default Country mapCountryToEnum(String country) {
        return country != null ? Country.fromDescription(country) : null;
    }

    default String mapNatureOfControlToString(NatureOfControl natureOfControl) {
        return natureOfControl != null ? natureOfControl.getDescription() : null;
    }

    default NatureOfControl mapNatureOfControlToEnum(String natureOfControl) {
        return natureOfControl != null ? NatureOfControl.fromDescription(natureOfControl) : null;
    }

    // List
    default List<String> mapNatureOfControlListToStringList(List<NatureOfControl> natures) {
        return natures == null ? null : Lists.newArrayList(
                natures.stream()
                        .map(this::mapNatureOfControlToString)
                        .toList()
        );
    }

    default List<NatureOfControl> mapStringListToNatureOfControlList(List<String> natures) {
        return natures == null ? null : Lists.newArrayList(
                natures.stream()
                        .map(this::mapNatureOfControlToEnum)
                        .toList()
        );
    }

}
