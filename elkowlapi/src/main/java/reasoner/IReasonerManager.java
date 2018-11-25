package reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface IReasonerManager {

    void addAxiomToOntology(OWLAxiom axiom);

    void addAxiomsToOntology(Set<OWLAxiom> axioms);

    void removeAxiomFromOntology(OWLAxiom axiom);

    void removeAxiomsFromOntology(Set<OWLAxiom> axioms);

    boolean isOntologyConsistent();

    void updateOntology(OWLOntology ontology);

}
