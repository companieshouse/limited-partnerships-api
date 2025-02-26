package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;

@Component
@Mapper(componentModel = "spring")
public interface GeneralPartnerMapper {
    GeneralPartnerDto daoToDto(GeneralPartnerDao dao);
    GeneralPartnerDao dtoToDao(GeneralPartnerDto dto);

    default String mapNationalityToString(Nationality nationality) {
        return nationality.getDescription();
    }

    default Nationality mapNationalityToEnum(String nationality) {
        return nationality != null ? Nationality.fromDescription(nationality) : null;
    }
}
