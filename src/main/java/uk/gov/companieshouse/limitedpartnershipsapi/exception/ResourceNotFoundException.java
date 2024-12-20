package uk.gov.companieshouse.limitedpartnershipsapi.exception;

public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
