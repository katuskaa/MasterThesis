package algorithms.mergeXPlain;

import common.Loader;
import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

class MergeXPlainHelper {

    boolean isBaseNotConsistent(Loader loader, OWLOntology ontology) {
        loader.updateOntology(ontology);

        return !loader.isOntologyConsistent();
    }

    boolean isBaseWithLiteralsConsistent(Loader loader, OWLOntology ontology, Literals literals) {
        addAxiomsToBase(loader, ontology, literals.getOwlAxioms());
        boolean isConsistent = loader.isOntologyConsistent();
        removeAxiomsFromBase(loader, ontology, literals.getOwlAxioms());

        return isConsistent;
    }

    void addAxiomsToBase(Loader loader, OWLOntology base, Set<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(base, axioms);
        loader.updateOntology(base);
    }

    void removeAxiomsFromBase(Loader loader, OWLOntology base, Set<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(base, axioms);
        loader.updateOntology(base);
    }

    Literals[] divideIntoSets(Literals literals) {
        int count = 0;
        int half = literals.getOwlAxioms().size() / 2;
        Literals[] dividedLiterals = new Literals[2];
        dividedLiterals[0] = new Literals();
        dividedLiterals[1] = new Literals();

        for (OWLAxiom owlAxiom : literals.getOwlAxioms()) {
//            if (count < half) {
//                dividedLiterals[0].getTBoxAxioms().add(owlAxiom);
//            } else {
//                dividedLiterals[1].getTBoxAxioms().add(owlAxiom);
//            }

            dividedLiterals[count % 2].getOwlAxioms().add(owlAxiom);
            count++;
        }

        return dividedLiterals;
    }

}
