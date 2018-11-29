package algorithms.mergeXPlain;

import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import reasoner.IReasonerManager;

import java.util.ArrayList;
import java.util.List;


class MergeXPlainHelper {

    boolean isBaseNotConsistent(IReasonerManager reasonerManager) {
        return !reasonerManager.isOntologyConsistent();
    }

    boolean isBaseWithLiteralsConsistent(IReasonerManager reasonerManager, Literals literals) {
        reasonerManager.addAxiomsToOntology(literals.getOwlAxioms());
        boolean isConsistent = reasonerManager.isOntologyConsistent();
        reasonerManager.removeAxiomsFromOntology(literals.getOwlAxioms());

        return isConsistent;
    }

    List<Literals> divideIntoSets(Literals literals) {
        List<Literals> dividedLiterals = new ArrayList<>();

        dividedLiterals.add(new Literals());
        dividedLiterals.add(new Literals());

        int count = 0;

        for (OWLAxiom owlAxiom : literals.getOwlAxioms()) {
            dividedLiterals.get(count % 2).getOwlAxioms().add(owlAxiom);
            count++;
        }

        return dividedLiterals;
    }

}
