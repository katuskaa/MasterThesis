package reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;


public class ReasonerManager implements IReasonerManager {

    private ILoader loader;

    public ReasonerManager(ILoader loader) {
        this.loader = loader;
    }

    @Override
    public void addAxiomToOntology(OWLAxiom axiom) {
        loader.getOntologyManager().addAxiom(loader.getOntology(), axiom);
        updateOntology(loader.getOntology());
    }

    @Override
    public void addAxiomsToOntology(Set<OWLAxiom> axioms) {
        loader.getOntologyManager().addAxioms(loader.getOntology(), axioms);
        updateOntology(loader.getOntology());
    }

    @Override
    public void removeAxiomFromOntology(OWLAxiom axiom) {
        loader.getOntologyManager().removeAxiom(loader.getOntology(), axiom);
        updateOntology(loader.getOntology());
    }

    @Override
    public void removeAxiomsFromOntology(Set<OWLAxiom> axioms) {
        loader.getOntologyManager().removeAxioms(loader.getOntology(), axioms);
        updateOntology(loader.getOntology());
    }

    @Override
    public boolean isOntologyConsistent() {
        return loader.getReasoner().isConsistent();
    }

    @Override
    public void updateOntology(OWLOntology ontology) {
        loader.getReasoner().dispose();
        loader.setOWLReasoner(loader.getReasonerFactory().createReasoner(ontology));
    }

}
