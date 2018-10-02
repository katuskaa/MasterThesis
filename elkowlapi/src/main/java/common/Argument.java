package common;

public enum Argument {

    INPUT_FILE("-f", "--file", true),
    OBSERVATION("-o", "--observation", true),
    HELP("-h", "--help", false);

    private final String shortName;
    private final String longName;
    private final Boolean mandatory;

    Argument(String shortName, String longName, boolean mandatory) {
        this.shortName = shortName;
        this.longName = longName;
        this.mandatory = mandatory;
    }

    public static Integer getCountOfMandatory() {
        Integer count = 0;
        for (Argument argument : Argument.values()) {
            if (argument.isMandatory()) {
                count++;
            }
        }
        return count;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getLongName() {
        return this.longName;
    }

    public Boolean isMandatory() {
        return this.mandatory;
    }
}
