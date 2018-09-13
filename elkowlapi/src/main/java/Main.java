import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Main {

    public static void main(String[] args) throws OWLOntologyCreationException {

        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        Ontology ontology = new Ontology();
        ontology.testFunctionality();
    }
}
