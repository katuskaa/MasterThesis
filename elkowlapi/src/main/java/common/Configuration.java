package common;

import reasoner.ReasonerType;
import reasoner.Strategy;

public class Configuration {
    public static String OBSERVATION = "";
    public final static String DELIMITER_OBSERVATION = ";";
    public static String INPUT_FILE = "";
    public static String INDIVIDUAL = "";
    public static ReasonerType REASONER;
    public final static String DELIMITER_ASSERTION = ":";
    public final static String DELIMITER_OBJECT_PROPERTY = ",";
    public final static String DELIMITER_INDIVIDUAL = ",";
    public final static String DELIMITER_ONTOLOGY = "#";
    public static Boolean MULTI_OBSERVATION = false;
    public static Strategy STRATEGY;
}
