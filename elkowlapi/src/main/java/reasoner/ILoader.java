package reasoner;

import models.Individuals;
import models.KnowledgeBase;
import models.Observation;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


public interface ILoader {

    void initialize(ReasonerType reasonerType);

    void changeReasoner(ReasonerType reasonerType);

    void initializeReasoner();

    KnowledgeBase getKnowledgeBase();

    Observation getObservation();

    void addObservation(OWLAxiom observation);

    Observation getNegObservation();

    void addNegObservation(OWLAxiom negObservation);

    OWLOntologyManager getOntologyManager();

    OWLOntology getOntology();

    OWLReasoner getReasoner();

    void setOWLReasonerFactory(OWLReasonerFactory reasonerFactory);

    String getOntologyIRI();

    OWLDataFactory getDataFactory();

    Individuals getIndividuals();

    void addNamedIndividual(OWLNamedIndividual namedIndividual);
}
