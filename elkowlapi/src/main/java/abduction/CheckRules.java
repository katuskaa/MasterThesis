package abduction;

import models.Explanation;
import models.KnowledgeBase;
import models.Observation;

public class CheckRules {

    private KnowledgeBase knowledgeBase;
    private Observation negObservation;
    private Explanation explanation;

    public CheckRules(KnowledgeBase knowledgeBase, Observation negObservation, Explanation explanation) {
        this.knowledgeBase = knowledgeBase;
        this.negObservation = negObservation;
        this.explanation = explanation;
    }

    public boolean isConsistent() {
        //spravit negaciu vysvetlenia, ak sa nachadza v knowledge base return false else true
        return true;
    }

    public boolean isRelevant() {
        //ak sa vysvetlenie zhoduje s observation, return false else true
        return true;
    }

    public boolean isInconsistent() {
        return true;
    }

}
