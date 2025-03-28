package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipSubmissionDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnershipValidator {

    @Autowired
    private Validator validator;


    public List<String> validate(LimitedPartnershipSubmissionDto limitedPartnershipDto) {
        Set<ConstraintViolation<LimitedPartnershipSubmissionDto>> violations = validator.validate(limitedPartnershipDto);

        List<String> errorsList = new ArrayList<>();
        violations.stream().forEach(v -> errorsList.add(v.getMessage()));

        // TODO These checks are valid for Partnership Types PFLP and SPFLP (the LP7D CHIPS transaction). Code will
        //      need changing when other Partnership Types need to be validated and sent to CHIPS

        final DataDto dataDto = limitedPartnershipDto.getData();
        if (PartnershipType.PFLP.equals(dataDto.getPartnershipType())
                || PartnershipType.SPFLP.equals(dataDto.getPartnershipType())) {
            if (dataDto.getEmail() == null) {
                errorsList.add("Email is required");
            }

            if (dataDto.getJurisdiction() == null) {
                errorsList.add("Jurisdiction is required");
            }

            if (dataDto.getRegisteredOfficeAddress() == null) {
                errorsList.add("Registered office address is required");
            }

            if (dataDto.getPrincipalPlaceOfBusinessAddress() == null) {
                errorsList.add("Principal place of business address is required");
            }

            if (dataDto.getTerm() != null) {
                errorsList.add("Term is not required");
            }
        }

        return errorsList;
    }
}
