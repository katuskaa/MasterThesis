package algorithms.mergeXPlain;

import models.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

class Conflict {

    private Set<OWLAxiom> literals;
    private Set<Explanation> explanations;

    Conflict(Set<OWLAxiom> literals, Set<Explanation> explanations) {
        this.literals = literals;
        this.explanations = explanations;
    }

    Set<OWLAxiom> getLiterals() {
        if (literals == null) {
            literals = new HashSet<>();
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
