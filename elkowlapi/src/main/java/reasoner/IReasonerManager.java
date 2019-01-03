package reasoner;

import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collection;

public interface IReasonerManager {

    void addAxiomToOntology(OWLAxiom axiom);

    void addAxiomsToOntology(Collection<OWLAxiom> axioms);

    void removeAxiomFromOntology(OWLAxiom axiom);

    void removeAxiomsFromOntology(Collection<OWLAxiom> axioms);

    boolean isOntologyConsistent();

    boolean isOntologyWithLiteralsConsistent(Literals literals);

}
