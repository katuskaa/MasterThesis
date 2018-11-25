package algorithms.mergeXPlain;

import common.Configuration;
import common.Printer;
import models.Literals;
import org.semanticweb.owlapi.model.*;
import reasoner.ILoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DataProcessing implements IDataProcessing {

    private ILoader loader;
    private Set<OWLClass> owlClasses;

    DataProcessing(ILoader loader) {
        this.loader = loader;
        this.owlClasses = new HashSet<>();
    }

    @Override
    public Literals getLiterals() {
        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(owlAxiom -> {

            Set<OWLClass> classes = owlAxiom.getClassesInSignature();
            owlClasses.addAll(classes);
        });

        return createLiterals();
    }

    private Literals createLiterals() {
        Literals literals = new Literals();

        literals.addLiterals(createLiteralsFromClasses());
        // literals.addLiterals(createLiteralsFromObjectProperties());

        return literals;
    }

    private Literals createLiteralsFromClasses() {
        Literals literals = new Literals();

        for (OWLClass owlClass : owlClasses) {
            String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
            OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();
            OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(Configuration.INDIVIDUAL)));

            OWLAxiom owlAxiom = dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual);
            OWLAxiom observation = loader.getObservation().getOwlAxiom();

            if (!Printer.print(owlAxiom).equals(Printer.print(observation))) {
                literals.getOwlAxioms().add(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
            }

            literals.getOwlAxioms().add(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
            literals.getOwlAxioms().add(dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
        }

        return literals;
    }

    private Literals createLiteralsFromObjectProperties() {
        Literals literals = new Literals();

        String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
        OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();

        loader.getOntology().axioms().forEach(axiom -> {
            Set<OWLClass> classes = axiom.getClassesInSignature();
            Set<OWLObjectProperty> objectProperties = axiom.getObjectPropertiesInSignature();

            if (classes.size() == 2 && objectProperties.size() == 1) {
                for (OWLObjectProperty owlObjectProperty : objectProperties) {

                    List<OWLClass> classList = new ArrayList<>(classes);

                    OWLNamedIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(classList.get(0).getIRI().getFragment())));
                    OWLNamedIndividual object = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(classList.get(1).getIRI().getFragment())));
                    OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRI.concat("#").concat(owlObjectProperty.getNamedProperty().getIRI().getFragment())));

                    literals.getOwlAxioms().add(dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
                    literals.getOwlAxioms().add(dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
                }
            }
        });

        return literals;
    }

}
