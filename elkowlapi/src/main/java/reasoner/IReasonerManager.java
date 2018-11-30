package reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public interface IReasonerManager {

    void addAxiomToOntology(OWLAxiom axiom);

    void addAxiomsToOntology(Set<OWLAxiom> axioms);

    void removeAxiomFromOntology(OWLAxiom axiom);

    void removeAxiomsFromOntology(Set<OWLAxiom> axioms);

    boolean isOntologyConsistent();

    boolean isSatisfiable(OWLAxiom axiom);

}
