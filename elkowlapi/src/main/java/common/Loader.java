package common;

import application.Application;
import application.ExitCode;
import models.KnowledgeBase;
import models.Observation;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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

//            reasonerFactory = new ReasonerFactory();
//            reasonerFactory = new JFactFactory();
            reasonerFactory = OpenlletReasonerFactory.getInstance();
//            reasonerFactory = new ElkReasonerFactory();

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

    private void initializeReasoner() {
        reasoner = reasonerFactory.createReasoner(ontology);
        logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_LOADED);
    }

    public void updateOntology(OWLOntology ontology) {
        reasoner.dispose();
        reasoner = reasonerFactory.createReasoner(ontology);
    }

    private void loadKnowledgeBase() {
        Set<OWLAxiom> TBoxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> ABoxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);

        knowledgeBase = new KnowledgeBase(TBoxAxioms, ABoxAxioms);
    }

    private void loadObservation() {
        ObservationParser observationParser = new ObservationParser(this);
        observationParser.parse();

        logger.log(Level.INFO, "Observation = ".concat(observation.toString()));
        logger.log(Level.INFO, "Negative observation = ".concat(negObservation.toString()));
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

    public OWLOntology getOntology() {
        return ontology;
    }

    void setObservation(Observation observation) {
        this.observation = observation;
    }

    void setNegObservation(Observation negObservation) {
        this.negObservation = negObservation;
    }

}
