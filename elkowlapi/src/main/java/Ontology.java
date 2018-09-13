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

public class Ontology {

    public void testFunctionality() throws OWLOntologyCreationException {

        // initialization
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(new File("files/family.owl"));
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

        // loading knowledge base
        Set<OWLAxiom> TBoxAxioms = ontology.getTBoxAxioms(Imports.INCLUDED);

        // dostanem observation


        // zacnem vytvarat strom

        reasoner.dispose();

    }

}
