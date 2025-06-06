package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Nationality {
    AFGHAN("Afghan"),
    ALBANIAN("Albanian"),
    ALGERIAN("Algerian"),
    AMERICAN("American"),
    ANDORRAN("Andorran"),
    ANGOLAN("Angolan"),
    ANGUILLAN("Anguillan"),
    ARGENTINE("Argentine"),
    ARMENIAN("Armenian"),
    AUSTRALIAN("Australian"),
    AUSTRIAN("Austrian"),
    AZERBAIJANI("Azerbaijani"),
    BAHAMIAN("Bahamian"),
    BAHRAINI("Bahraini"),
    BANGLADESHI("Bangladeshi"),
    BARBADIAN("Barbadian"),
    BELARUSIAN("Belarusian"),
    BELGIAN("Belgian"),
    BELIZEAN("Belizean"),
    BENINESE("Beninese"),
    BERMUDIAN("Bermudian"),
    BHUTANESE("Bhutanese"),
    BOLIVIAN("Bolivian"),
    BOTSWANAN("Botswanan"),
    BRAZILIAN("Brazilian"),
    BRITISH("British"),
    BRITISH_VIRGIN_ISLANDER("British Virgin Islander"),
    BRUNEIAN("Bruneian"),
    BULGARIAN("Bulgarian"),
    BURKINAN("Burkinan"),
    BURMESE("Burmese"),
    BURUNDIAN("Burundian"),
    CAMBODIAN("Cambodian"),
    CAMEROONIAN("Cameroonian"),
    CANADIAN("Canadian"),
    CAPE_VERDEAN("Cape Verdean"),
    CAYMAN_ISLANDER("Cayman Islander"),
    CENTRAL_AFRICAN("Central African"),
    CHADIAN("Chadian"),
    CHILEAN("Chilean"),
    CHINESE("Chinese"),
    CITIZEN_OF_ANTIGUA_AND_BARBUDA("Citizen of Antigua and Barbuda"),
    CITIZEN_BOSNIA_AND_HERZEGOVINA("Citizen of Bosnia and Herzegovina"),
    CITIZEN_OF_GUINEA_BISSAU("Citizen of Guinea-Bissau"),
    CITIZEN_OF_KIRIBATI("Citizen of Kiribati"),
    CITIZEN_OF_SEYCHELLES("Citizen of Seychelles"),
    CITIZEN_OF_THE_DOMINICAN_REPUBLIC("Citizen of the Dominican Republic"),
    CITIZEN_OF_VANUATU("Citizen of Vanuatu"),
    COLOMBIAN("Colombian"),
    COMORAN("Comoran"),
    CONGOLESE("Congolese (Congo)"),
    CONGOLESE_DRC("Congolese (DRC)"),
    COOK_ISLANDER("Cook Islander"),
    COSTA_RICAN("Costa Rican"),
    CROATIAN("Croatian"),
    CUBAN("Cuban"),
    CYMRAES("Cymraes"),
    CYMRO("Cymro"),
    CYPRIOT("Cypriot"),
    CZECH("Czech"),
    DANISH("Danish"),
    DJIBOUTIAN("Djiboutian"),
    DOMINICAN("Dominican"),
    DUTCH("Dutch"),
    EAST_TIMORESE("East Timorese"),
    ECUADOREAN("Ecuadorean"),
    EGYPTIAN("Egyptian"),
    EMIRATI("Emirati"),
    ENGLISH("English"),
    EQUATORIAL_GUINEAN("Equatorial Guinean"),
    ERITREAN("Eritrean"),
    ESTONIAN("Estonian"),
    ETHIOPIAN("Ethiopian"),
    FAROESE("Faroese"),
    FIJIAN("Fijian"),
    FILIPINO("Filipino"),
    FINNISH("Finnish"),
    FRENCH("French"),
    GABONESE("Gabonese"),
    GAMBIAN("Gambian"),
    GEORGIAN("Georgian"),
    GERMAN("German"),
    GHANAIAN("Ghanaian"),
    GIBRALTARIAN("Gibraltarian"),
    GREEK("Greek"),
    GREENLANDIC("Greenlandic"),
    GRENADIAN("Grenadian"),
    GUAMANIAN("Guamanian"),
    GUATEMALAN("Guatemalan"),
    GUINEAN("Guinean"),
    GUYANESE("Guyanese"),
    HAITIAN("Haitian"),
    HONDURAN("Honduran"),
    HONG_KONGER("Hong Konger"),
    HUNGARIAN("Hungarian"),
    ICELANDIC("Icelandic"),
    INDIAN("Indian"),
    INDONESIAN("Indonesian"),
    IRANIAN("Iranian"),
    IRAQI("Iraqi"),
    IRISH("Irish"),
    ISRAELI("Israeli"),
    ITALIAN("Italian"),
    IVORIAN("Ivorian"),
    JAMAICAN("Jamaican"),
    JAPANESE("Japanese"),
    JORDANIAN("Jordanian"),
    KAZAKH("Kazakh"),
    KENYAN("Kenyan"),
    KITTITIAN("Kittitian"),
    KOSOVAN("Kosovan"),
    KUWAITI("Kuwaiti"),
    KYRGYZ("Kyrgyz"),
    LAO("Lao"),
    LATVIAN("Latvian"),
    LEBANESE("Lebanese"),
    LIBERIAN("Liberian"),
    LIBYAN("Libyan"),
    LIECHTENSTEIN_CITIZEN("Liechtenstein citizen"),
    LITHUANIAN("Lithuanian"),
    LUXEMBOURGER("Luxembourger"),
    MACANESE("Macanese"),
    MACEDONIAN("Macedonian"),
    MALAGASY("Malagasy"),
    MALAWIAN("Malawian"),
    MALAYSIAN("Malaysian"),
    MALDIVIAN("Maldivian"),
    MALIAN("Malian"),
    MALTESE("Maltese"),
    MARSHALLESE("Marshallese"),
    MARTINIQUAIS("Martiniquais"),
    MAURITANIAN("Mauritanian"),
    MAURITIAN("Mauritian"),
    MEXICAN("Mexican"),
    MICRONESIAN("Micronesian"),
    MOLDOVAN("Moldovan"),
    MONEGASQUE("Monegasque"),
    MONGOLIAN("Mongolian"),
    MONTENEGRIN("Montenegrin"),
    MONTSERRATIAN("Montserratian"),
    MOROCCAN("Moroccan"),
    MOSOTHO("Mosotho"),
    MOZAMBICAN("Mozambican"),
    NAMIBIAN("Namibian"),
    NAURUAN("Nauruan"),
    NEPALESE("Nepalese"),
    NEW_ZEALANDER("New Zealander"),
    NICARAGUAN("Nicaraguan"),
    NIGERIAN("Nigerian"),
    NIGERIEN("Nigerien"),
    NIUEAN("Niuean"),
    NORTH_KOREAN("North Korean"),
    NORTHERN_IRISH("Northern Irish"),
    NORWEGIAN("Norwegian"),
    OMANI("Omani"),
    PAKISTANI("Pakistani"),
    PALAUAN("Palauan"),
    PALESTINIAN("Palestinian"),
    PANAMANIAN("Panamanian"),
    PAPUA_NEW_GUINEAN("Papua New Guinean"),
    PARAGUAYAN("Paraguayan"),
    PERUVIAN("Peruvian"),
    PITCAIRN_ISLANDER("Pitcairn Islander"),
    POLISH("Polish"),
    PORTUGUESE("Portuguese"),
    PRYDEINIG("Prydeinig"),
    PUERTO_RICAN("Puerto Rican"),
    QATARI("Qatari"),
    ROMANIAN("Romanian"),
    RUSSIAN("Russian"),
    RWANDAN("Rwandan"),
    SALVADOREAN("Salvadorean"),
    SAMMARINESE("Sammarinese"),
    SAMOAN("Samoan"),
    SAO_TOMEAN("Sao Tomean"),
    SAUDI_ARABIAN("Saudi Arabian"),
    SCOTTISH("Scottish"),
    SENEGALESE("Senegalese"),
    SERBIAN("Serbian"),
    SIERRA_LEONEAN("Sierra Leonean"),
    SINGAPOREAN("Singaporean"),
    SLOVAK("Slovak"),
    SLOVENIAN("Slovenian"),
    SOLOMON_ISLANDER("Solomon Islander"),
    SOMALI("Somali"),
    SOUTH_AFRICAN("South African"),
    SOUTH_KOREAN("South Korean"),
    SOUTH_SUDANESE("South Sudanese"),
    SPANISH("Spanish"),
    SRI_LANKAN("Sri Lankan"),
    ST_HELENIAN("St Helenian"),
    ST_LUCIAN("St Lucian"),
    STATELESS("Stateless"),
    SUDANESE("Sudanese"),
    SURINAMESE("Surinamese"),
    SWAZI("Swazi"),
    SWEDISH("Swedish"),
    SWISS("Swiss"),
    SYRIAN("Syrian"),
    TAIWANESE("Taiwanese"),
    TAJIK("Tajik"),
    TANZANIAN("Tanzanian"),
    THAI("Thai"),
    TOGOLESE("Togolese"),
    TONGAN("Tongan"),
    TRINIDADIAN("Trinidadian"),
    TRISTANIAN("Tristanian"),
    TUNISIAN("Tunisian"),
    TURKISH("Turkish"),
    TURKMEN("Turkmen"),
    TURKS_AND_CAICOS_ISLANDER("Turks and Caicos Islander"),
    TUVALUAN("Tuvaluan"),
    UGANDAN("Ugandan"),
    UKRAINIAN("Ukrainian"),
    URUGUAYAN("Uruguayan"),
    UZBEK("Uzbek"),
    VATICAN_CITIZEN("Vatican citizen"),
    VENEZUELAN("Venezuelan"),
    VIETNAMESE("Vietnamese"),
    VINCENTIAN("Vincentian"),
    WALLISIAN("Wallisian"),
    WELSH("Welsh"),
    YEMENI("Yemeni"),
    ZAMBIAN("Zambian"),
    ZIMBABWEAN("Zimbabwean"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");


    private final String description;

    Nationality(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static Nationality fromDescription(String description) {
        for (Nationality nationality : Nationality.values()) {
            if (nationality.getDescription().equalsIgnoreCase(description)) {
                return nationality;
            }
        }

        return Nationality.UNKNOWN;
    }
}
