package algorithms.abduction;

import models.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.Set;

public class CheckRules implements ICheckRules {

    private ILoader loader;
    private IReasonerManager reasonerManager;

    CheckRules(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;
    }

    @Override
    public boolean isConsistent(Explanation explanation) {
        reasonerManager.addAxiomsToOntology(explanation.getOwlAxioms());

        boolean isConsistent = reasonerManager.isOntologyConsistent();

        reasonerManager.removeAxiomsFromOntology(explanation.getOwlAxioms());

        return isConsistent;
    }

    @Override
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

    @Override
    public boolean isInconsistent(Explanation explanation) {
        reasonerManager.addAxiomToOntology(loader.getNegObservation().getOwlAxiom());
        reasonerManager.addAxiomsToOntology(explanation.getOwlAxioms());

        boolean isConsistent = reasonerManager.isOntologyConsistent();

        reasonerManager.removeAxiomFromOntology(loader.getNegObservation().getOwlAxiom());
        reasonerManager.removeAxiomsFromOntology(explanation.getOwlAxioms());

        return !isConsistent;
    }

}