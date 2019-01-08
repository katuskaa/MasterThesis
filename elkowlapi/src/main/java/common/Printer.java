package common;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Printer {

    public static String print(OWLAxiom owlAxiom) {
        if (owlAxiom instanceof OWLClassAssertionAxiom) {
            return Printer.getNamedIndividual(owlAxiom).concat(DLSyntax.DELIMITER_ASSERTION).concat(Printer.getClassAssertionAxiom(owlAxiom));
        }

        return Printer.getNamedIndividual(owlAxiom).concat(DLSyntax.DELIMITER_ASSERTION).concat(Printer.getObjectPropertyAssertionAxiom(owlAxiom));
    }

    private static String getNamedIndividual(OWLAxiom owlAxiom) {
        List<String> owlNamedIndividuals = new ArrayList<>();
        List<OWLNamedIndividual> individualsInSignature = owlAxiom.individualsInSignature().collect(Collectors.toList());

        for (OWLNamedIndividual owlNamedIndividual : individualsInSignature) {
            owlNamedIndividuals.add(owlNamedIndividual.getIRI().getFragment());
        }

        return StringUtils.join(owlNamedIndividuals, DLSyntax.DELIMITER_INDIVIDUAL);
    }

    private static String getClassAssertionAxiom(OWLAxiom owlAxiom) {
        List<OWLAxiom> axioms = new ArrayList<>();
        axioms.add(owlAxiom);

        Set<OWLAxiom> classAssertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.CLASS_ASSERTION);
        List<String> classAssertions = new ArrayList<>();

        for (OWLAxiom classAssertionAxiom : classAssertionAxioms) {
            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) classAssertionAxiom).getClassExpression();

            if (owlClassExpression instanceof OWLClass) {
                classAssertions.add(owlClassExpression.asOWLClass().getIRI().getFragment());
            } else if (owlClassExpression instanceof OWLObjectComplementOf) {
                classAssertions.add("¬" + owlClassExpression.getComplementNNF().asOWLClass().getIRI().getFragment());
            }
        }

        return StringUtils.join(classAssertions, " ");
    }

    private static String getObjectPropertyAssertionAxiom(OWLAxiom owlAxiom) {
        List<OWLAxiom> axioms = new ArrayList<>();
        axioms.add(owlAxiom);

        Set<OWLAxiom> objectPropertyAssertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.OBJECT_PROPERTY_ASSERTION);
        Set<OWLAxiom> negativeObjectPropertyAssertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION);

        List<String> objectPropertyAssertions = new ArrayList<>();

        for (OWLAxiom objectPropertyAssertionAxiom : objectPropertyAssertionAxioms) {
            objectPropertyAssertionAxiom.objectPropertiesInSignature().forEach(objectProperty -> {
                objectPropertyAssertions.add(objectProperty.getIRI().getFragment());
            });
        }

        for (OWLAxiom negativeObjectPropertyAssertionAxiom : negativeObjectPropertyAssertionAxioms) {
            negativeObjectPropertyAssertionAxiom.objectPropertiesInSignature().forEach(objectProperty -> {
                objectPropertyAssertions.add("¬" + objectProperty.getIRI().getFragment());
            });
        }

        return StringUtils.join(objectPropertyAssertions, " ");
    }
}
