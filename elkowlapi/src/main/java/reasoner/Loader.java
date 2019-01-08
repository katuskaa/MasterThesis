package reasoner;

import application.Application;
import application.ExitCode;
import common.Configuration;
import common.IObservationParser;
import common.LogMessage;
import common.ObservationParser;
import models.Individuals;
import models.Observation;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader implements ILoader {

    private Logger logger = Logger.getLogger(Loader.class.getSimpleName());

    private OWLOntologyManager ontologyManager;
    private OWLReasonerFactory reasonerFactory;
    private OWLOntology ontology;
    private OWLReasoner reasoner;

    private Observation observation;
    private Observation negObservation;
    private String ontologyIRI;
    private Individuals namedIndividuals;

    @Override
    public void initialize(ReasonerType reasonerType) {
        loadReasoner(reasonerType);
        loadObservation();
    }

    private void loadReasoner(ReasonerType reasonerType) {
        try {
            ontologyManager = OWLManager.createOWLOntologyManager();
            ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Configuration.INPUT_FILE));

            changeReasoner(reasonerType);
            initializeReasoner();

            if (reasoner.isConsistent()) {
                logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_CONSISTENCY);
            } else {
                logger.log(Level.WARNING, LogMessage.ERROR_ONTOLOGY_CONSISTENCY);
                reasoner.dispose();

                Application.finish(ExitCode.ERROR);
            }

        } catch (OWLOntologyCreationException exception) {
            logger.log(Level.WARNING, LogMessage.ERROR_CREATING_ONTOLOGY, exception);
            Application.finish(ExitCode.ERROR);
        }
    }

    @Override
    public void changeReasoner(ReasonerType reasonerType) {
        switch (reasonerType) {
            case ELK:
                setOWLReasonerFactory(new ElkReasonerFactory());
                break;

            case PELLET:
                setOWLReasonerFactory(OpenlletReasonerFactory.getInstance());
                break;

            case HERMIT:
                setOWLReasonerFactory(new ReasonerFactory());
                break;

            case JFACT:
                setOWLReasonerFactory(new JFactFactory());
                break;
        }

        reasoner = reasonerFactory.createReasoner(ontology);
        logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_LOADED);
    }

    @Override
    public void initializeReasoner() {
        reasoner.flush();
    }

    private void loadObservation() {
        namedIndividuals = new Individuals();

        IObservationParser observationParser = new ObservationParser(this);
        observationParser.parse();

        logger.log(Level.INFO, "individuals = ".concat(namedIndividuals.toString()));

        logger.log(Level.INFO, "Observation = ".concat(observation.toString()));
        logger.log(Level.INFO, "Negative observation = ".concat(negObservation.toString()));
    }

    @Override
    public Observation getObservation() {
        return observation;
    }

    @Override
    public void setObservation(OWLAxiom observation) {
        this.observation = new Observation(observation);
    }

    @Override
    public Observation getNegObservation() {
        return negObservation;
    }

    @Override
    public void setNegObservation(OWLAxiom negObservation) {
        this.negObservation = new Observation(negObservation);
    }

    @Override
    public OWLOntologyManager getOntologyManager() {
        return ontologyManager;
    }

    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public OWLReasoner getReasoner() {
        return reasoner;
    }

    @Override
    public void setOWLReasonerFactory(OWLReasonerFactory reasonerFactory) {
        this.reasonerFactory = reasonerFactory;
    }

    @Override
    public String getOntologyIRI() {
        if (ontologyIRI == null) {
            ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
        }

        return ontologyIRI;
    }

    @Override
    public OWLDataFactory getDataFactory() {
        return ontologyManager.getOWLDataFactory();
    }

    @Override
    public Individuals getIndividuals() {
        return namedIndividuals;
    }

    @Override
    public void addNamedIndividual(OWLNamedIndividual namedIndividual) {
        namedIndividuals.addNamedIndividual(namedIndividual);
    }

}
