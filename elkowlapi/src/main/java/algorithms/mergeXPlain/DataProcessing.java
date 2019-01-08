package algorithms.mergeXPlain;

import common.Printer;
import models.Literals;
import org.semanticweb.owlapi.model.*;
import reasoner.ILoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class DataProcessing implements IDataProcessing {

    private ILoader loader;
    private List<OWLClass> owlClasses;
    private List<OWLObjectProperty> owlObjectProperties;

    DataProcessing(ILoader loader) {
        this.loader = loader;

        this.owlClasses = new ArrayList<>();
        this.owlObjectProperties = new ArrayList<>();
    }

    @Override
    public Literals getLiterals() {
        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(owlAxiom -> {
            owlClasses.addAll(owlAxiom.classesInSignature().collect(Collectors.toList()));
            owlObjectProperties.addAll(owlAxiom.objectPropertiesInSignature().collect(Collectors.toList()));
        });

        return createLiterals();
    }

    private Literals createLiterals() {
        Literals literals = new Literals();

        literals.addLiterals(createLiteralsFromClasses());
        literals.addLiterals(createLiteralsFromObjectProperties());

        return literals;
    }

    private Literals createLiteralsFromClasses() {
        Literals literals = new Literals();
        OWLAxiom observation = loader.getObservation().getOwlAxiom();

        for (OWLClass owlClass : owlClasses) {
            for (OWLNamedIndividual namedIndividual : loader.getIndividuals().getNamedIndividuals()) {
                OWLAxiom owlAxiom = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual);

                if (!Printer.print(owlAxiom).equals(Printer.print(observation))) {
                    literals.getOwlAxioms().add(owlAxiom);
                }

                literals.getOwlAxioms().add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
            }
        }

        return literals;
    }

    private Literals createLiteralsFromObjectProperties() {
        Literals literals = new Literals();
        OWLAxiom observation = loader.getObservation().getOwlAxiom();

        for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
            for (OWLNamedIndividual subject : loader.getIndividuals().getNamedIndividuals()) {
                for (OWLNamedIndividual object : loader.getIndividuals().getNamedIndividuals()) {

                    if (!subject.equals(object)) {
                        OWLAxiom owlAxiom = loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(owlObjectProperty, subject, object);

                        if (!Printer.print(owlAxiom).equals(Printer.print(observation))) {
                            literals.getOwlAxioms().add(owlAxiom);
                        }

                        literals.getOwlAxioms().add(loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(owlObjectProperty, subject, object));
                    }
                }
            }
        }

        return literals;
    }

}
