package reasoner;

import models.KnowledgeBase;
import models.Observation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public interface ILoader {

    void initialize(ReasonerType reasonerType);

    void changeReasoner(ReasonerType reasonerType);

    void initializeReasoner();

    KnowledgeBase getKnowledgeBase();

    Observation getObservation();

    void setObservation(Observation observation);

    Observation getNegObservation();

    void setNegObservation(Observation negObservation);

    OWLOntologyManager getOntologyManager();

    OWLOntology getOntology();

    OWLReasoner getReasoner();

    void setOWLReasoner(OWLReasoner reasoner);

    OWLReasonerFactory getReasonerFactory();

    void setOWLReasonerFactory(OWLReasonerFactory reasonerFactory);

}
