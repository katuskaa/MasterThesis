package common;

import org.semanticweb.owlapi.model.*;
import reasoner.Loader;

import java.util.ArrayList;
import java.util.List;


public class ObservationParser implements IObservationParser {

    private Loader loader;

    public ObservationParser(Loader loader) {
        this.loader = loader;
    }

    @Override
    public void parse() {
        String[] observations;

        if (Configuration.MULTI_OBSERVATION) {
            observations = Configuration.OBSERVATION.split(Configuration.DELIMITER_OBSERVATION);
        } else {
            observations = new String[1];
            observations[0] = Configuration.OBSERVATION;
        }

        for (String observation : observations) {
            String[] expressions = observation.split(DLSyntax.DELIMITER_ASSERTION);

            if (expressions[0].contains(DLSyntax.DELIMITER_OBJECT_PROPERTY)) {
                parseObjectProperty(expressions);
            } else {
                parseClassAssertion(expressions);
            }

            Configuration.INDIVIDUAL = expressions[0];
        }
    }

    private void parseClassAssertion(String[] expressions) {
        OWLNamedIndividual namedIndividual = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(expressions[0])));
        OWLClass owlClass = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(expressions[1])));

        loader.addNamedIndividual(namedIndividual);

        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(namedIndividual));

        loader.setObservation(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
        loader.setNegObservation(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
    }

    private void parseObjectProperty(String[] expressions) {
        String[] individuals = expressions[0].split(DLSyntax.DELIMITER_OBJECT_PROPERTY);

        OWLNamedIndividual subject = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(individuals[0])));
        OWLNamedIndividual object = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(individuals[1])));
        OWLObjectProperty objectProperty = loader.getDataFactory().getOWLObjectProperty(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(expressions[1])));


        List<OWLNamedIndividual> alreadyInOntology = new ArrayList<>();

        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {
            if (OWLNamedIndividual.class.isAssignableFrom(axiom.getEntity().getClass())) {
                alreadyInOntology.add((OWLNamedIndividual) axiom.getEntity());
            }
        });

        if (!alreadyInOntology.contains(subject)) {
            loader.addNamedIndividual(subject);
        }

        if (!alreadyInOntology.contains(object)) {
            loader.addNamedIndividual(object);
        }

        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(subject));
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(object));

        loader.setObservation(loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
        loader.setNegObservation(loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
    }

}
