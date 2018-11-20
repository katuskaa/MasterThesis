package common;

import models.Observation;
import org.semanticweb.owlapi.model.*;


class ObservationParser {

    private Loader loader;

    ObservationParser(Loader loader) {
        this.loader = loader;
    }

    void parse() {
        String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
        OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();

        String[] expressions = Configuration.OBSERVATION.split(":");

        if (expressions[0].contains(",")) {
            parseObjectProperty(ontologyIRI, dataFactory, expressions);
        } else {
            parseClassAssertion(ontologyIRI, dataFactory, expressions);
        }

        Configuration.INDIVIDUAL = expressions[0];
    }

    private void parseClassAssertion(String ontologyIRI, OWLDataFactory dataFactory, String[] expressions) {
        OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(expressions[0])));
        OWLClass owlClass = dataFactory.getOWLClass(IRI.create(ontologyIRI.concat("#").concat(expressions[1])));

        loader.setObservation(new Observation(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual)));
        loader.setNegObservation(new Observation(dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual)));
    }

    private void parseObjectProperty(String ontologyIRI, OWLDataFactory dataFactory, String[] expressions) {
        String[] individuals = expressions[0].split(",");

        OWLNamedIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(individuals[0])));
        OWLNamedIndividual object = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(individuals[1])));
        OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRI.concat("#").concat(expressions[1])));

        loader.setObservation(new Observation(dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object)));
        loader.setNegObservation(new Observation(dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object)));
    }
}
