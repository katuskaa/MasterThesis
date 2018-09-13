import org.apache.log4j.BasicConfigurator;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Main {

    public static void main(String[] args) throws OWLOntologyCreationException {

        BasicConfigurator.configure();

        Ontology ontology = new Ontology();
        ontology.testFunctionality();
    }
}
