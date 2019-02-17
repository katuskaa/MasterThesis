package reasoner;

import common.DLSyntax;
import common.Printer;
import org.semanticweb.owlapi.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AxiomManager {

    public static List<OWLAxiom> createClassAssertionAxiom(ILoader loader, OWLAxiom axiom, boolean preserveObservation) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (OWLDeclarationAxiom.class.isAssignableFrom(axiom.getClass()) && OWLClass.class.isAssignableFrom(((OWLDeclarationAxiom) axiom).getEntity().getClass())) {
            String name = ((OWLDeclarationAxiom) axiom).getEntity().getIRI().getFragment();
            OWLClass owlClass = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));

            String className = Printer.getClassAssertionAxiom(loader.getObservation().getOwlAxiom());
            boolean containsNegation = className.contains(DLSyntax.DISPLAY_NEGATION);

            if (containsNegation) {
                className = className.substring(1);
            }

            for (OWLNamedIndividual namedIndividual : loader.getIndividuals().getNamedIndividuals()) {

                if (!preserveObservation) {
                    if (!name.equals(className)) {
                        owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                        owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
                    } else {
                        if (containsNegation) {
                            owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                        } else {
                            owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
                        }
                    }
                } else {
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
                }
            }
        }

        return owlAxioms;
    }

    public static List<OWLAxiom> createObjectPropertyAssertionAxiom(ILoader loader, OWLAxiom axiom) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (OWLDeclarationAxiom.class.isAssignableFrom(axiom.getClass()) && OWLObjectProperty.class.isAssignableFrom(((OWLDeclarationAxiom) axiom).getEntity().getClass())) {
            String name = ((OWLDeclarationAxiom) axiom).getEntity().getIRI().getFragment();
            OWLObjectProperty objectProperty = loader.getDataFactory().getOWLObjectProperty(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));

            for (OWLNamedIndividual subject : loader.getIndividuals().getNamedIndividuals()) {
                for (OWLNamedIndividual object : loader.getIndividuals().getNamedIndividuals()) {
                    if (!subject.equals(object)) {
                        owlAxioms.add(loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
                        owlAxioms.add(loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
                    }
                }
            }
        }

        return owlAxioms;
    }

    public static OWLAxiom getComplementOfOWLAxiom(ILoader loader, OWLAxiom owlAxiom) {
        Set<OWLClass> names = owlAxiom.classesInSignature().collect(Collectors.toSet());
        String name = "";
        OWLAxiom complement = null;

        if (names.size() == 1) {
            name = names.iterator().next().getIRI().getFragment();
            OWLClass owlClass = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));

            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) owlAxiom).getClassExpression();

            if (OWLObjectComplementOf.class.isAssignableFrom(owlClassExpression.getClass())) {
                complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, ((OWLClassAssertionAxiom) owlAxiom).getIndividual());
            } else {
                complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), ((OWLClassAssertionAxiom) owlAxiom).getIndividual());
            }

        } else {

            if (OWLObjectPropertyAssertionAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
                OWLObjectPropertyExpression owlObjectProperty = ((OWLObjectPropertyAssertionAxiom) owlAxiom).getProperty();
                complement = loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLObjectPropertyAssertionAxiom) owlAxiom).getObject());

            } else if (OWLNegativeObjectPropertyAssertionAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
                OWLObjectPropertyExpression owlObjectProperty = ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getProperty();
                complement = loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getObject());
            }
        }

        return complement;
    }

}
