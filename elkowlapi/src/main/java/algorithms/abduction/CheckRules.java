package algorithms.abduction;

import models.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.Collection;
import java.util.List;

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
        Collection<OWLAxiom> explanations = explanation.getOwlAxioms();

//        for (OWLAxiom observation : loader.getObservation().getOwlAxiom()) {
            for (OWLAxiom axiom : explanations) {
                if (loader.getObservation().getOwlAxiom().equals(axiom)) {
                    return false;
                }
            }
//        }

        return true;
    }

    @Override
    public boolean isExplanation(Explanation explanation) {
        reasonerManager.addAxiomToOntology(loader.getNegObservation().getOwlAxiom());
        reasonerManager.addAxiomsToOntology(explanation.getOwlAxioms());

        boolean isConsistent = reasonerManager.isOntologyConsistent();

        reasonerManager.removeAxiomFromOntology(loader.getNegObservation().getOwlAxiom());
        reasonerManager.removeAxiomsFromOntology(explanation.getOwlAxioms());

        return !isConsistent;
    }

    @Override
    public boolean isMinimal(List<Explanation> explanationList, Explanation explanation) {
        if (explanation == null || !(explanation.getOwlAxioms() instanceof List)) {
            return false;
        }

        OWLAxiom lastAxiom = ((List<OWLAxiom>) explanation.getOwlAxioms()).get(explanation.getOwlAxioms().size() - 1);

        for (Explanation minimalExplanation : explanationList) {
            if (minimalExplanation.getOwlAxioms().contains(lastAxiom)) {
                return false;
            }
        }

        return true;
    }

}