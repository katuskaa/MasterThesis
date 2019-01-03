package reasoner;

import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collection;

public class ReasonerManager implements IReasonerManager {

    private ILoader loader;

    public ReasonerManager(ILoader loader) {
        this.loader = loader;
    }

    @Override
    public void addAxiomToOntology(OWLAxiom axiom) {
        loader.getOntologyManager().addAxiom(loader.getOntology(), axiom);
        loader.initializeReasoner();
    }

    @Override
    public void addAxiomsToOntology(Collection<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        loader.initializeReasoner();
    }

    @Override
    public void removeAxiomFromOntology(OWLAxiom axiom) {
        loader.getOntologyManager().removeAxiom(loader.getOntology(), axiom);
        loader.initializeReasoner();
    }

    @Override
    public void removeAxiomsFromOntology(Collection<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(loader.getOntology(), axioms);
        loader.initializeReasoner();
    }

    @Override
    public boolean isOntologyConsistent() {
        loader.initializeReasoner();
        return loader.getReasoner().isConsistent();
    }


    @Override
    public boolean isOntologyWithLiteralsConsistent(Literals literals) {
        addAxiomsToOntology(literals.getOwlAxioms());
        boolean isConsistent = isOntologyConsistent();
        removeAxiomsFromOntology(literals.getOwlAxioms());

        return isConsistent;
    }

}
