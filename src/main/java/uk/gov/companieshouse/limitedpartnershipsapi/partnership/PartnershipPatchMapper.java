package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.JsonNullableMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;

@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface PartnershipPatchMapper {

    DataDto map(PartnershipPatchDto patchDto);

    @InheritConfiguration
    void update(PartnershipPatchDto update, @MappingTarget DataDto destination);
}
