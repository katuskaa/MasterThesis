package common;

import models.KnowledgeBase;
import models.Observation;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader {

    private OWLOntologyManager ontologyManager;
    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private KnowledgeBase knowledgeBase;
    private Observation observation;
    private Observation negObservation;

    public void initialize() {
        loadReasoner();
        loadKnowledgeBase();
        loadObservation();
    }

    private void loadReasoner() {
        try {
            ontologyManager = OWLManager.createOWLOntologyManager();
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Configuration.INPUT_FILE));
            reasoner = reasonerFactory.createReasoner(ontology);
        } catch (OWLOntologyCreationException exception) {
            Logger.getGlobal().log(Level.WARNING, LogMessage.ERROR_CREATING_ONTOLOGY);
            System.exit(0);
        }
    }

    private void loadKnowledgeBase() {
        Set<OWLAxiom> TBoxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        knowledgeBase = new KnowledgeBase(TBoxAxioms);
    }

    private void loadObservation() {
        String ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();
        String[] expressions = Configuration.OBSERVATION.split(":");
        OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(expressions[0])));
        OWLClass owlClass = dataFactory.getOWLClass(IRI.create(ontologyIRI.concat("#").concat(expressions[1])));
        observation = new Observation(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
        negObservation = new Observation(dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
    }

    public void disposeReasoner() {
        reasoner.dispose();
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public Observation getObservation() {
        return observation;
    }

    public Observation getNegObservation() {
        return negObservation;
    }

}
