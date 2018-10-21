package algorithms.mergeXPlain;

import models.Explanation;
import models.Literals;

import java.util.HashSet;
import java.util.Set;

class Conflict {

    private Literals literals;
    private Set<Explanation> explanations;

    Conflict() {
        this.literals = new Literals();
        this.explanations = new HashSet<>();
    }

    Conflict(Literals literals, Set<Explanation> explanations) {
        this.literals = literals;
        this.explanations = explanations;
    }

    Literals getLiterals() {
        if (literals == null) {
            literals = new Literals(new HashSet<>());
        }
        return literals;
    }

    Set<Explanation> getExplanations() {
        if (explanations == null) {
            explanations = new HashSet<>();
        }
        return explanations;
    }
}
