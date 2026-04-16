package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;

@Component
public class PersonWithSignificantControlValidatorFactory {
    private final IndividualPersonValidator individualPersonValidator;
    private final OtherRegistrablePersonValidator otherRegistrablePersonValidator;
    private final RelevantLegalEntityValidator relevantLegalEntityValidator;
    private final UnknownTypeValidator unknownTypeValidator;

    @Autowired
    public PersonWithSignificantControlValidatorFactory(IndividualPersonValidator individualPersonValidator,
                                                        OtherRegistrablePersonValidator otherRegistrablePersonValidator,
                                                        RelevantLegalEntityValidator relevantLegalEntityValidator,
                                                        UnknownTypeValidator unknownTypeValidator) {
        this.individualPersonValidator = individualPersonValidator;
        this.otherRegistrablePersonValidator = otherRegistrablePersonValidator;
        this.relevantLegalEntityValidator = relevantLegalEntityValidator;
        this.unknownTypeValidator = unknownTypeValidator;
    }
    
    public PersonWithSignificantControlValidator getValidator(PersonWithSignificantControlType type) {
        if (type == null) {
            throw new IllegalArgumentException("PersonWithSignificantControlType must not be null");
        }
        return switch (type) {
            case INDIVIDUAL_PERSON -> individualPersonValidator;
            case OTHER_REGISTRABLE_PERSON -> otherRegistrablePersonValidator;
            case RELEVANT_LEGAL_ENTITY -> relevantLegalEntityValidator;
            default -> unknownTypeValidator;
        };
    }
}
