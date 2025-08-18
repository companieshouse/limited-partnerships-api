package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import org.springframework.stereotype.Component;

@Component
public class FilingKind {
    private static final String SEPARATOR = "#";

    public String getSubKind(String kind) {
        String[] filingKind = kind.split(SEPARATOR);

        return filingKind[1];
    }

    public String addSubKind(String kind, String resourceKind) {
        String subKind = getSubKind(resourceKind);

        return kind + SEPARATOR + subKind;
    }
}