package reasoner;

import base.Configuration;
import base.LogMessage;
import models.KnowledgeBase;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
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

public class ReasonerManager {

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private KnowledgeBase knowledgeBase;

    public void initializeReasoner() {
        try {
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(Configuration.INPUT_FILE));
            reasoner = reasonerFactory.createReasoner(ontology);
        } catch (OWLOntologyCreationException exception) {
            Logger.getGlobal().log(Level.WARNING, LogMessage.ERROR_CREATING_ONTOLOGY);
            System.exit(0);
        }
    }

    public void loadKnowledgeBase() {
        Set<OWLAxiom> TBoxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        knowledgeBase = new KnowledgeBase(TBoxAxioms);
    }

    public void loadObservation() {

    }

    public void loadNegObservation() {

    }

    public void disposeReasoner() {
        reasoner.dispose();
    }

}
