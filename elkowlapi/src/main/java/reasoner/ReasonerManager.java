package reasoner;

import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

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

    public boolean isOntologySatisfiable(Literals literals) {
        loader.initializeReasoner();
        boolean isSatisfiable = true;

        for (OWLAxiom axiom : literals.getOwlAxioms()) {
            try {
                isSatisfiable = loader.getReasoner().isSatisfiable(((OWLClassAssertionAxiom) axiom).getClassExpression());
            } catch (Exception exception) {
                isSatisfiable = false;
            }

            if (!isSatisfiable) {
                break;
            }
        }

        return isSatisfiable;
    }

    @Override
    public boolean isOntologyWithLiteralsConsistent(Literals literals) {
        addAxiomsToOntology(literals.getOwlAxioms());
        boolean isConsistent = isOntologyConsistent();
        boolean isSatisfiable = isOntologySatisfiable(literals);
        removeAxiomsFromOntology(literals.getOwlAxioms());

        return isSatisfiable;
    }

}
