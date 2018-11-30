package reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;

import java.util.Set;


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
    public void addAxiomsToOntology(Set<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        loader.initializeReasoner();
    }

    @Override
    public void removeAxiomFromOntology(OWLAxiom axiom) {
        loader.getOntologyManager().removeAxiom(loader.getOntology(), axiom);
        loader.initializeReasoner();
    }

    @Override
    public void removeAxiomsFromOntology(Set<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(loader.getOntology(), axioms);
        loader.initializeReasoner();
    }

    @Override
    public boolean isOntologyConsistent() {
        loader.initializeReasoner();
        return loader.getReasoner().isConsistent();
    }

    @Override
    public boolean isSatisfiable(OWLAxiom axiom) {
        OWLClassExpression classExpression = ((OWLClassAssertionAxiom) axiom).getClassExpression();
        boolean isSatisfiable;

        try {
            isSatisfiable = loader.getReasoner().isSatisfiable(classExpression);
        } catch (InconsistentOntologyException exception) {
            isSatisfiable = false;
        }

        return isSatisfiable;
    }

}
