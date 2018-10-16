package algorithms.mergeXPlain;

import common.Loader;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

class DataProcessing {

    private Loader loader;
    private Set<OWLClass> owlClasses;

    DataProcessing(Loader loader) {
        this.loader = loader;
    }

    Set<OWLAxiom> getLiterals() {
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

        Set<OWLAxiom> literals = createLiterals();
        return literals;
    }

    private void addClassesFromClassExpressions(Set<OWLClassExpression> classExpressions) {
        for (OWLClassExpression classExpression : classExpressions) {
            if (classExpression instanceof OWLClass) {
                owlClasses.add(classExpression.asOWLClass());
            }
        }
    }

    private Set<OWLAxiom> createLiterals() {
        Set<OWLAxiom> literals = new HashSet<>();

        for (OWLClass owlClass : owlClasses) {
            String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
            OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();
            OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat("individual")));
            literals.add(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
            literals.add(dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
        }

        return literals;
    }

}
