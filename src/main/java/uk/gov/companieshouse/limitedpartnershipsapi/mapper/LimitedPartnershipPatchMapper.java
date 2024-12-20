package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipPatchDto;

@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface LimitedPartnershipPatchMapper {

    DataDto map(LimitedPartnershipPatchDto patchDto);

    @InheritConfiguration
    void update(LimitedPartnershipPatchDto update, @MappingTarget DataDto destination);
}
