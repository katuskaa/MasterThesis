package algorithms.mergeXPlain;

import models.Explanation;
import models.Literals;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

class Conflict {

    private Literals literals;
    private List<Explanation> explanations;

    Conflict() {
        this.literals = new Literals();
        this.explanations = new LinkedList<>();
    }

    Conflict(Literals literals, List<Explanation> explanations) {
        this.literals = literals;
        this.explanations = explanations;
    }

    Literals getLiterals() {
        if (literals == null) {
            literals = new Literals(new HashSet<>());
        }
        return literals;
    }

    List<Explanation> getExplanations() {
        if (explanations == null) {
            explanations = new LinkedList<>();
        }
        return explanations;
    }
}
