package reasoner;

public enum ReasonerType {
    ELK("elk"),
    PELLET("pellet"),
    HERMIT("hermit"),
    JFACT("jfact");

    String reasonerName = "";

    ReasonerType(String reasonerName) {
        this.reasonerName = reasonerName;
    }

    public static ReasonerType getReasonerTypeByName(String reasonerName) {
        if (reasonerName == null) {
            return null;
        }

        for (ReasonerType reasonerType : ReasonerType.values()) {
            if (reasonerType.getReasonerName().equals(reasonerName)) {
                return reasonerType;
            }
        }

        return null;
    }

    public String getReasonerName() {
        return reasonerName;
    }
}
