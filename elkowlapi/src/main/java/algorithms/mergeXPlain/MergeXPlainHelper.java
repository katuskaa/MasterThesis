package algorithms.mergeXPlain;

import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import reasoner.ILoader;
import reasoner.IReasonerManager;

import java.util.Set;

class MergeXPlainHelper {

    boolean isBaseNotConsistent(IReasonerManager reasonerManager, OWLOntology ontology) {
        reasonerManager.updateOntology(ontology);
        return !reasonerManager.isOntologyConsistent();
    }

    boolean isBaseWithLiteralsConsistent(ILoader loader, IReasonerManager reasonerManager, OWLOntology ontology, Literals literals) {
        addAxiomsToBase(loader, reasonerManager, ontology, literals.getOwlAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        removeAxiomsFromBase(loader, reasonerManager, ontology, literals.getOwlAxioms());

        return isConsistent;
    }

    void addAxiomsToBase(ILoader loader, IReasonerManager reasonerManager, OWLOntology base, Set<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(base, axioms);
        reasonerManager.updateOntology(base);
    }

    void removeAxiomsFromBase(ILoader loader, IReasonerManager reasonerManager, OWLOntology base, Set<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(base, axioms);
        reasonerManager.updateOntology(base);
    }

    Literals[] divideIntoSets(Literals literals) {
        Literals[] dividedLiterals = new Literals[2];

        dividedLiterals[0] = new Literals();
        dividedLiterals[1] = new Literals();

        int count = 0;

        for (OWLAxiom owlAxiom : literals.getOwlAxioms()) {
            dividedLiterals[count % 2].getOwlAxioms().add(owlAxiom);
            count++;
        }

        return dividedLiterals;
    }

}
