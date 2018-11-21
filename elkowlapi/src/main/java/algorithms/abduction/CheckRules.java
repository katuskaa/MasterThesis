package algorithms.abduction;

import common.Loader;
import models.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public class CheckRules {

    private Loader loader;

    public CheckRules(Loader loader) {
        this.loader = loader;
    }

    public boolean isConsistent(Explanation explanation) {
        loader.addAxiomsToOntology(explanation.getOwlAxioms());
        boolean isConsistent = loader.isOntologyConsistent();
        loader.removeAxiomsFromOntology(explanation.getOwlAxioms());

        return isConsistent;
    }

    public boolean isRelevant(Explanation explanation) {
        Set<OWLAxiom> explanations = explanation.getOwlAxioms();
        OWLAxiom observation = loader.getObservation().getOwlAxiom();

        for (OWLAxiom axiom : explanations) {
            if (axiom.equals(observation)) {
                return false;
            }
        }

        return true;
    }

    public boolean isExplanation(Explanation explanation) {
        loader.addAxiomsToOntology(explanation.getOwlAxioms());
        loader.addAxiomToOntology(loader.getNegObservation().getOwlAxiom());

        boolean isConsistent = loader.isOntologyConsistent();

        loader.removeAxiomsFromOntology(explanation.getOwlAxioms());
        loader.removeAxiomFromOntology(loader.getNegObservation().getOwlAxiom());

        return isConsistent;
    }

}
