package reasoner;

import models.Literals;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;
import java.util.stream.Stream;

public interface IReasonerManager {

    void addAxiomToOntology(OWLAxiom axiom);

    void addAxiomsToOntology(Collection<OWLAxiom> axioms);

    void removeAxiomFromOntology(OWLAxiom axiom);

    void resetOntology(Stream<OWLAxiom> axioms);

    boolean isOntologyConsistent();

    boolean isOntologyWithLiteralsConsistent(Literals literals, OWLOntology ontology);

}
