package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Country {
    AFGHANISTAN("Afghanistan"),
    ALAND_ISLANDS("Aland Islands"),
    ALBANIA("Albania"),
    ALGERIA("Algeria"),
    ALDERNEY("Alderney"),
    AMERICAN_SAMOA("American Samoa"),
    ANDORRA("Andorra"),
    ANGOLA("Angola"),
    ANGUILLA("Anguilla"),
    ANTARCTICA("Antarctica"),
    ANTIGUA_AND_BARBUDA("Antigua and Barbuda"),
    ARGENTINA("Argentina"),
    ARMENIA("Armenia"),
    ARUBA("Aruba"),
    AUSTRALIA("Australia"),
    AUSTRIA("Austria"),
    AZERBAIJAN("Azerbaijan"),
    BAHAMAS("Bahamas"),
    BAHRAIN("Bahrain"),
    BANGLADESH("Bangladesh"),
    BARBADOS("Barbados"),
    BELARUS("Belarus"),
    BELGIUM("Belgium"),
    BELIZE("Belize"),
    BENIN("Benin"),
    BERMUDA("Bermuda"),
    BHUTAN("Bhutan"),
    BOLIVIA("Bolivia"),
    BONAIRE_SINT_EUSTATIUS_AND_SABA("Bonaire, Sint Eustatius and Saba"),
    BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina"),
    BOTSWANA("Botswana"),
    BOUVET_ISLAND("Bouvet Island"),
    BRAZIL("Brazil"),
    BRITISH_INDIAN_OCEAN_TERRITORY("British Indian Ocean Territory"),
    BRUNEI_DARUSSALAM("Brunei Darussalam"),
    BULGARIA("Bulgaria"),
    BURKINA_FASO("Burkina Faso"),
    BURUNDI("Burundi"),
    CAMBODIA("Cambodia"),
    CAMEROON("Cameroon"),
    CANADA("Canada"),
    CAPE_VERDE("Cape Verde"),
    CAYMAN_ISLANDS("Cayman Islands"),
    CENTRAL_AFRICAN_REPUBLIC("Central African Republic"),
    CHAD("Chad"),
    CHILE("Chile"),
    CHINA("China"),
    CHRISTMAS_ISLAND("Christmas Island"),
    COCOS_KEELING_ISLANDS("Cocos (Keeling) Islands"),
    COLOMBIA("Colombia"),
    COMOROS("Comoros"),
    CONGO("Congo"),
    CONGO_DEMOCRATIC_REPUBLIC("Congo, the Democratic Republic of the"),
    COOK_ISLANDS("Cook Islands"),
    COSTA_RICA("Costa Rica"),
    CROATIA("Croatia"),
    CUBA("Cuba"),
    CURACAO("Curacao"),
    CYPRUS("Cyprus"),
    CZECH_REPUBLIC("Czech Republic"),
    DENMARK("Denmark"),
    DJIBOUTI("Djibouti"),
    DOMINICA("Dominica"),
    DOMINICAN_REPUBLIC("Dominican Republic"),
    EAST_TIMOR("East Timor"),
    ECUADOR("Ecuador"),
    EGYPT("Egypt"),
    EL_SALVADOR("El Salvador"),
    ENGLAND("England"),
    EQUATORIAL_GUINEA("Equatorial Guinea"),
    ERITREA("Eritrea"),
    ESTONIA("Estonia"),
    ESWATINI("Eswatini"),
    ETHIOPIA("Ethiopia"),
    FALKLAND_ISLANDS("Falkland Islands"),
    FAROE_ISLANDS("Faroe Islands"),
    FIJI("Fiji"),
    FINLAND("Finland"),
    FRANCE("France"),
    FRENCH_GUIANA("French Guiana"),
    FRENCH_POLYNESIA("French Polynesia"),
    FRENCH_SOUTHERN_TERRITORIES("French Southern Territories"),
    GABON("Gabon"),
    GAMBIA("Gambia"),
    GEORGIA("Georgia"),
    GERMANY("Germany"),
    GHANA("Ghana"),
    GIBRALTAR("Gibraltar"),
    GREECE("Greece"),
    GREENLAND("Greenland"),
    GRENADA("Grenada"),
    GUADELOUPE("Guadeloupe"),
    GUAM("Guam"),
    GUATEMALA("Guatemala"),
    GUERNSEY("Guernsey"),
    GUINEA("Guinea"),
    GUINEA_BISSAU("Guinea-Bissau"),
    GUYANA("Guyana"),
    HAITI("Haiti"),
    HEARD_ISLAND_AND_MCDONALD_ISLANDS("Heard Island and McDonald Islands"),
    HERM("Herm"),
    HONDURAS("Honduras"),
    HONG_KONG("Hong Kong"),
    HUNGARY("Hungary"),
    ICELAND("Iceland"),
    INDIA("India"),
    INDONESIA("Indonesia"),
    IRAN("Iran"),
    IRAQ("Iraq"),
    IRELAND("Ireland"),
    ISLE_OF_MAN("Isle of Man"),
    ISRAEL("Israel"),
    ITALY("Italy"),
    IVORY_COAST("Ivory Coast"),
    JAMAICA("Jamaica"),
    JAPAN("Japan"),
    JERSEY("Jersey"),
    JORDAN("Jordan"),
    KAZAKHSTAN("Kazakhstan"),
    KENYA("Kenya"),
    KIRIBATI("Kiribati"),
    KOSOVO("Kosovo"),
    KUWAIT("Kuwait"),
    KYRGYZSTAN("Kyrgyzstan"),
    LAOS("Laos"),
    LATVIA("Latvia"),
    LEBANON("Lebanon"),
    LESOTHO("Lesotho"),
    LIBERIA("Liberia"),
    LIBYA("Libya"),
    LIECHTENSTEIN("Liechtenstein"),
    LITHUANIA("Lithuania"),
    LUXEMBOURG("Luxembourg"),
    MACAO("Macao"),
    MADAGASCAR("Madagascar"),
    MALAWI("Malawi"),
    MALAYSIA("Malaysia"),
    MALDIVES("Maldives"),
    MALI("Mali"),
    MALTA("Malta"),
    MARSHALL_ISLANDS("Marshall Islands"),
    MARTINIQUE("Martinique"),
    MAURITANIA("Mauritania"),
    MAURITIUS("Mauritius"),
    MAYOTTE("Mayotte"),
    MEXICO("Mexico"),
    MICRONESIA("Micronesia"),
    MOLDOVA("Moldova"),
    MONACO("Monaco"),
    MONGOLIA("Mongolia"),
    MONTENEGRO("Montenegro"),
    MONTSERRAT("Montserrat"),
    MOROCCO("Morocco"),
    MOZAMBIQUE("Mozambique"),
    MYANMAR("Myanmar"),
    NAMIBIA("Namibia"),
    NAURU("Nauru"),
    NEPAL("Nepal"),
    NETHERLANDS("Netherlands"),
    NEW_CALEDONIA("New Caledonia"),
    NEW_ZEALAND("New Zealand"),
    NICARAGUA("Nicaragua"),
    NIGER("Niger"),
    NIGERIA("Nigeria"),
    NIUE("Niue"),
    NORFOLK_ISLAND("Norfolk Island"),
    NORTH_KOREA("North Korea"),
    NORTHERN_IRELAND("Northern Ireland"),
    NORTHERN_MARIANA_ISLANDS("Northern Mariana Islands"),
    NORWAY("Norway"),
    OMAN("Oman"),
    PAKISTAN("Pakistan"),
    PALAU("Palau"),
    PALESTINE_STATE_OF("Palestine, State of"),
    PANAMA("Panama"),
    PAPUA_NEW_GUINEA("Papua New Guinea"),
    PARAGUAY("Paraguay"),
    PERU("Peru"),
    PHILIPPINES("Philippines"),
    PITCAIRN("Pitcairn"),
    POLAND("Poland"),
    PORTUGAL("Portugal"),
    PUERTO_RICO("Puerto Rico"),
    QATAR("Qatar"),
    REUNION("Reunion"),
    ROMANIA("Romania"),
    RUSSIA("Russia"),
    RWANDA("Rwanda"),
    SAINT_BARTHELEMY("Saint Barthelemy"),
    SAINT_HELENA_ASCENSION_AND_TRISTAN_DA_CUNHA("Saint Helena, Ascension and Tristan da Cunha"),
    SAINT_KITTS_AND_NEVIS("Saint Kitts and Nevis"),
    SAINT_LUCIA("Saint Lucia"),
    SAINT_MARTIN_FRENCH_PART("Saint Martin (French part)"),
    SAINT_PIERRE_AND_MIQUELON("Saint Pierre and Miquelon"),
    SAINT_VINCENT_AND_THE_GRENADINES("Saint Vincent and the Grenadines"),
    SAMOA("Samoa"),
    SAN_MARINO("San Marino"),
    SAO_TOME_AND_PRINCIPE("Sao Tome and Principe"),
    SARK("Sark"),
    SAUDI_ARABIA("Saudi Arabia"),
    SCOTLAND("Scotland"),
    SENEGAL("Senegal"),
    SERBIA("Serbia"),
    SEYCHELLES("Seychelles"),
    SIERRA_LEONE("Sierra Leone"),
    SINGAPORE("Singapore"),
    SINT_MAARTEN_DUTCH_PART("Sint Maarten (Dutch part)"),
    SLOVAKIA("Slovakia"),
    SLOVENIA("Slovenia"),
    SOLOMON_ISLANDS("Solomon Islands"),
    SOMALIA("Somalia"),
    SOUTH_AFRICA("South Africa"),
    SOUTH_GEORGIA_AND_THE_SOUTH_SANDWICH_ISLANDS("South Georgia and the South Sandwich Islands"),
    SOUTH_KOREA("South Korea"),
    SOUTH_SUDAN("South Sudan"),
    SPAIN("Spain"),
    SRI_LANKA("Sri Lanka"),
    SUDAN("Sudan"),
    SURINAME("Suriname"),
    SVALBARD_AND_JAN_MAYEN("Svalbard and Jan Mayen"),
    SWEDEN("Sweden"),
    SWITZERLAND("Switzerland"),
    SYRIA("Syria"),
    TAIWAN("Taiwan"),
    TAJIKISTAN("Tajikistan"),
    TANZANIA("Tanzania"),
    THAILAND("Thailand"),
    TOGO("Togo"),
    TOKELAU("Tokelau"),
    TONGA("Tonga"),
    TRINIDAD_AND_TOBAGO("Trinidad and Tobago"),
    TUNISIA("Tunisia"),
    TURKEY("Turkey"),
    TURKMENISTAN("Turkmenistan"),
    TURKS_AND_CAICOS_ISLANDS("Turks and Caicos Islands"),
    TUVALU("Tuvalu"),
    UGANDA("Uganda"),
    UKRAINE("Ukraine"),
    UNITED_ARAB_EMIRATES("United Arab Emirates"),
    UNITED_KINGDOM("United Kingdom"),
    UNITED_STATES("United States"),
    UNITED_STATES_MINOR_OUTLYING_ISLANDS("United States Minor Outlying Islands"),
    URUGUAY("Uruguay"),
    UZBEKISTAN("Uzbekistan"),
    VANUATU("Vanuatu"),
    VATICAN_CITY("Vatican City"),
    VENEZUELA("Venezuela"),
    VIETNAM("Vietnam"),
    VIRGIN_ISLANDS_BRITISH("Virgin Islands, British"),
    VIRGIN_ISLANDS_US("Virgin Islands, U.S."),
    WALES("Wales"),
    WALLIS_AND_FUTUNA("Wallis and Futuna"),
    WESTERN_SAHARA("Western Sahara"),
    YEMEN("Yemen"),
    ZAMBIA("Zambia"),
    ZIMBABWE("Zimbabwe"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    Country(String description) {
        this.description = description;
    }

    @JsonCreator
    public static Country fromDescription(String description) {
        for (Country country : Country.values()) {
            if (country.getDescription().equalsIgnoreCase(description)) {
                return country;
            }
        }

        return Country.UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

}