package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;

@Component
public class PersonWithSignificantControlValidator {
    private final IndividualPersonValidatorStrategy individualPersonValidatorStrategy;
    private final OtherRegistrablePersonValidatorStrategy otherRegistrablePersonValidatorStrategy;
    private final RelevantLegalEntityValidatorStrategy relevantLegalEntityValidatorStrategy;
    private final UnknownTypeValidatorStrategy unknownTypeValidatorStrategy;

    @Autowired
    public PersonWithSignificantControlValidator(IndividualPersonValidatorStrategy individualPersonValidatorStrategy,
                                                 OtherRegistrablePersonValidatorStrategy otherRegistrablePersonValidatorStrategy,
                                                 RelevantLegalEntityValidatorStrategy relevantLegalEntityValidatorStrategy,
                                                 UnknownTypeValidatorStrategy unknownTypeValidatorStrategy) {
        this.individualPersonValidatorStrategy = individualPersonValidatorStrategy;
        this.otherRegistrablePersonValidatorStrategy = otherRegistrablePersonValidatorStrategy;
        this.relevantLegalEntityValidatorStrategy = relevantLegalEntityValidatorStrategy;
        this.unknownTypeValidatorStrategy = unknownTypeValidatorStrategy;
    }
    
    public PersonWithSignificantControlValidatorStrategy getValidatorByType(PersonWithSignificantControlType type) {
        if (type == null) {
            throw new IllegalArgumentException("PersonWithSignificantControlType must not be null");
        }
        return switch (type) {
            case INDIVIDUAL_PERSON -> individualPersonValidatorStrategy;
            case OTHER_REGISTRABLE_PERSON -> otherRegistrablePersonValidatorStrategy;
            case RELEVANT_LEGAL_ENTITY -> relevantLegalEntityValidatorStrategy;
            default -> unknownTypeValidatorStrategy;
        };
    }
}
