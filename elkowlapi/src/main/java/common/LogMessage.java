package common;

public class LogMessage {
    public final static String INFO_HELP_MESSAGE = "Mandatory arguments: \n -f or --file inputPath\n -o or --observation individual:Concept or individual,individual:ObjectProperty";
    public final static String INFO_ONTOLOGY_LOADED = "Ontology successfully loaded";
    public final static String INFO_ONTOLOGY_CONSISTENCY = "Ontology is consistent";

    public final static String ERROR_MISSING_ARGUMENTS = "Mandatory arguments are -f or --file and -o or --observation. Use -h or --help.";
    public final static String ERROR_CREATING_ONTOLOGY = "Ontology can not be created";
    public final static String ERROR_ONTOLOGY_CONSISTENCY = "Ontology is not consistent";


}
