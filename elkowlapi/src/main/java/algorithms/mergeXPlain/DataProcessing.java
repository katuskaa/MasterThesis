package algorithms.mergeXPlain;

import common.Configuration;
import common.Loader;
import models.Literals;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

class DataProcessing {

    private Loader loader;
    private Set<OWLClass> owlClasses;

    DataProcessing(Loader loader) {
        this.loader = loader;
    }

    Literals getLiterals() {
        owlClasses = new HashSet<>();
        Set<OWLAxiom> axioms = loader.getKnowledgeBase().getOwlAxioms();

        for (OWLAxiom axiom : axioms) {

            if (axiom instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
                if (subClassOfAxiom.getSubClass() instanceof OWLClass) {
                    owlClasses.add(subClassOfAxiom.getSubClass().asOWLClass());
                }
                if (subClassOfAxiom.getSuperClass() instanceof OWLClass) {
                    owlClasses.add(subClassOfAxiom.getSuperClass().asOWLClass());
                }
            } else if (axiom instanceof OWLDisjointClassesAxiom) {
                Set<OWLClassExpression> classExpressions = ((OWLDisjointClassesAxiom) axiom).getClassExpressions();
                addClassesFromClassExpressions(classExpressions);
            } else if (axiom instanceof OWLEquivalentClassesAxiom) {
                Set<OWLClassExpression> classExpressions = ((OWLEquivalentClassesAxiom) axiom).getClassExpressions();
                addClassesFromClassExpressions(classExpressions);
            }
        }

        return createLiterals();
    }

    private void addClassesFromClassExpressions(Set<OWLClassExpression> classExpressions) {
        for (OWLClassExpression classExpression : classExpressions) {
            if (classExpression instanceof OWLClass) {
                owlClasses.add(classExpression.asOWLClass());
            }
        }
    }

    private Literals createLiterals() {
        Literals literals = new Literals();

        for (OWLClass owlClass : owlClasses) {
            String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
            OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();
            OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(Configuration.INDIVIDUAL)));

//            OWLAxiom owlAxiom = dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual);
//            OWLAxiom observation = loader.getObservation().getOwlAxiom();
//
//            if (!Printer.print(owlAxiom).equals(Printer.print(observation))) {
//                literals.getOwlAxioms().add(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
//            }

            literals.getOwlAxioms().add(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
            literals.getOwlAxioms().add(dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
        }

        return literals;
    }

}
