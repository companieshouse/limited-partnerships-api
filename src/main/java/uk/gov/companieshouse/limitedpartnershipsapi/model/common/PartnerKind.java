package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import java.util.Set;

public enum PartnerKind {
    ADD_GENERAL_PARTNER_PERSON("limited-partnership#add-general-partner-person"),
    ADD_GENERAL_PARTNER_LEGAL_ENTITY("limited-partnership#add-general-partner-legal-entity"),
    ADD_LIMITED_PARTNER_PERSON("limited-partnership#add-limited-partner-person"),
    ADD_LIMITED_PARTNER_LEGAL_ENTITY("limited-partnership#add-limited-partner-legal-entity"),
    REMOVE_GENERAL_PARTNER_PERSON("limited-partnership#remove-general-partner-person"),
    REMOVE_GENERAL_PARTNER_LEGAL_ENTITY("limited-partnership#remove-general-partner-legal-entity"),
    REMOVE_LIMITED_PARTNER_PERSON("limited-partnership#remove-limited-partner-person"),
    REMOVE_LIMITED_PARTNER_LEGAL_ENTITY("limited-partnership#remove-limited-partner-legal-entity"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    private static final Set<String> REMOVE_GENERAL_PARTNER_KINDS = Set.of(
            REMOVE_GENERAL_PARTNER_PERSON.getDescription(),
            REMOVE_GENERAL_PARTNER_LEGAL_ENTITY.getDescription()
    );

    private static final Set<String> REMOVE_LIMITED_PARTNER_KINDS = Set.of(
            REMOVE_LIMITED_PARTNER_PERSON.getDescription(),
            REMOVE_LIMITED_PARTNER_LEGAL_ENTITY.getDescription()
    );

    private static final Set<String> ADD_GENERAL_PARTNER_KINDS = Set.of(
            ADD_GENERAL_PARTNER_PERSON.getDescription(),
            ADD_GENERAL_PARTNER_LEGAL_ENTITY.getDescription()
    );

    private static final Set<String> ADD_LIMITED_PARTNER_KINDS = Set.of(
            ADD_LIMITED_PARTNER_PERSON.getDescription(),
            ADD_LIMITED_PARTNER_LEGAL_ENTITY.getDescription()
    );

    PartnerKind(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static PartnerKind fromDescription(String description) {
        for (PartnerKind kind : PartnerKind.values()) {
            if (kind.getDescription().equalsIgnoreCase(description)) {
                return kind;
            }
        }

        return PartnerKind.UNKNOWN;
    }

    public static boolean isRemoveGeneralPartnerKind(String kind) {
        return kind != null && REMOVE_GENERAL_PARTNER_KINDS.contains(kind);
    }

    public static boolean isRemoveLimitedPartnerKind(String kind) {
        return kind != null && REMOVE_LIMITED_PARTNER_KINDS.contains(kind);
    }

    public static boolean isAddPartnerKind(String kind) {
        return kind != null && (ADD_GENERAL_PARTNER_KINDS.contains(kind) || ADD_LIMITED_PARTNER_KINDS.contains(kind));
    }
}
