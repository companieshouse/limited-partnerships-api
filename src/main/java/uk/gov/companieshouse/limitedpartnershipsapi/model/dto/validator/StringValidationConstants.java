package uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator;

public interface StringValidationConstants {

    public static final String REG_EXP_FOR_INVALID_CHARACTERS = "^[-,.:; 0-9A-Z&@$£¥€'\"«»?!/\\\\()\\[\\]{}<>*=#%+ÀÁÂÃÄÅĀĂĄÆǼÇĆĈĊČÞĎÐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮİĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐǾŒŔŖŘŚŜŞŠŢŤŦÙÚÛÜŨŪŬŮŰŲŴẀẂẄỲÝŶŸŹŻŽa-zſƒǺàáâãäåāăąæǽçćĉċčþďðèéêëēĕėęěĝģğġĥħìíîïĩīĭįĵķĺļľŀłñńņňŋòóôõöøōŏőǿœŕŗřśŝşšţťŧùúûüũūŭůűųŵẁẃẅỳýŷÿźżž]*$";
    public static final int MIN_SIZE = 1;
    public static final String MIN_SIZE_MESSAGE = "value input must be greater than {min}";
    public static final int MAX_SIZE = 160;
    public static final String MAX_SIZE_MESSAGE = "value input must be less than {max}";

}
