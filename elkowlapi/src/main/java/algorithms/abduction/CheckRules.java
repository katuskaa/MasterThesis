package algorithms.abduction;

import models.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.Set;

public class CheckRules implements ICheckRules {

    private ILoader loader;
    private IReasonerManager reasonerManager;

    public CheckRules(ILoader loader, IReasonerManager reasonerManager) {
        this.loader = loader;
        this.reasonerManager = reasonerManager;
    }

    @Override
    public boolean isConsistent(Explanation explanation) {
        reasonerManager.addAxiomsToOntology(explanation.getOwlAxioms());
//        boolean isConsistent = reasonerManager.isOntologyConsistent();
        boolean isSatisfiable = false;

        // System.out.println(explanation);

        for (OWLAxiom axiom : explanation.getOwlAxioms()) {
            // System.out.println(((OWLClassAssertionAxiom) axiom).getClassExpression());

            try {
                isSatisfiable = loader.getReasoner().isSatisfiable(((OWLClassAssertionAxiom) axiom).getClassExpression());
            } catch (InconsistentOntologyException exception) {
                isSatisfiable = false;
            }

            if (!isSatisfiable) {
                break;
            }
        }

//        System.out.println("isConsistent = " + isConsistent);
//        System.out.println("isSatisfiable = " + isSatisfiable);
//        System.out.println();

        reasonerManager.removeAxiomsFromOntology(explanation.getOwlAxioms());

        return isSatisfiable;
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
        boolean isSatisfiable = false;

        System.out.println(explanation);

        for (OWLAxiom axiom : explanation.getOwlAxioms()) {
            System.out.println(((OWLClassAssertionAxiom) axiom).getClassExpression());

            try {
                isSatisfiable = loader.getReasoner().isSatisfiable(((OWLClassAssertionAxiom) axiom).getClassExpression());
            } catch (InconsistentOntologyException exception) {
                isSatisfiable = false;
            }

            if (!isSatisfiable) {
                break;
            }
        }

        System.out.println("isConsistent = " + isConsistent);
        System.out.println("isSatisfiable = " + isSatisfiable);
        System.out.println();

        reasonerManager.removeAxiomsFromOntology(explanation.getOwlAxioms());
        reasonerManager.removeAxiomFromOntology(loader.getNegObservation().getOwlAxiom());

        return !isSatisfiable;
    }

}
