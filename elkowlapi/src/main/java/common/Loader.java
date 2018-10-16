package common;

import application.Application;
import application.ExitCode;
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

    private Logger logger = Logger.getLogger(Loader.class.getName());

    private OWLOntologyManager ontologyManager;
    private OWLReasonerFactory reasonerFactory;
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
            reasonerFactory = new ElkReasonerFactory();
            ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Configuration.INPUT_FILE));
            initializeReasoner();

            if (isOntologyConsistent()) {
                logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_CONSISTENCY);
            } else {
                logger.log(Level.WARNING, LogMessage.ERROR_ONTOLOGY_CONSISTENCY);
                Application.finish(ExitCode.ERROR);
            }
        } catch (OWLOntologyCreationException exception) {
            logger.log(Level.WARNING, LogMessage.ERROR_CREATING_ONTOLOGY, exception);
            Application.finish(ExitCode.ERROR);
        }
    }

    public void initializeReasoner() {
        reasoner = reasonerFactory.createReasoner(ontology);
        logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_LOADED);
    }

    public void updateOntology(OWLOntology ontology) {
        this.ontology = ontology;
        reasoner.dispose();
        reasoner = reasonerFactory.createReasoner(ontology);
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
        Message.log("Observation", observation.toString());
        Message.log("Neg observation", negObservation.toString());
    }

    public boolean isOntologyConsistent() {
        return reasoner.isConsistent();
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

    public OWLOntologyManager getOntologyManager() {
        return ontologyManager;
    }

    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }
}
