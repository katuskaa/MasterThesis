package common;

import com.sun.deploy.util.StringUtils;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Printer {

    public static String print(OWLAxiom owlAxiom) {
        if (owlAxiom instanceof OWLClassAssertionAxiom) {
            return Printer.getNamedIndividual(owlAxiom).concat(":").concat(Printer.getClassAssertionAxiom(owlAxiom));
        }

        return Printer.getNamedIndividual(owlAxiom).concat(":").concat(Printer.getObjectPropertyAssertionAxiom(owlAxiom));
    }

    private static String getNamedIndividual(OWLAxiom owlAxiom) {
        List<String> owlNamedIndividuals = new ArrayList<>();
        Set<OWLNamedIndividual> individualsInSignature = owlAxiom.getIndividualsInSignature();
        for (OWLNamedIndividual owlNamedIndividual : individualsInSignature) {
            owlNamedIndividuals.add(owlNamedIndividual.getIRI().getFragment());
        }

        return StringUtils.join(owlNamedIndividuals, ",");
    }

    private static String getClassAssertionAxiom(OWLAxiom owlAxiom) {
        Set<OWLAxiom> axioms = new HashSet<>();
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
        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(owlAxiom);

        Set<OWLAxiom> objectPropertyAssertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.OBJECT_PROPERTY_ASSERTION);
        Set<OWLAxiom> negativeObjectPropertyAssertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION);

        List<String> objectPropertyAssertions = new ArrayList<>();

        for (OWLAxiom objectPropertyAssertionAxiom : objectPropertyAssertionAxioms) {
            objectPropertyAssertionAxiom.getObjectPropertiesInSignature().forEach(objectProperty -> {
                objectPropertyAssertions.add(objectProperty.getIRI().getFragment());
            });
        }

        for (OWLAxiom negativeObjectPropertyAssertionAxiom : negativeObjectPropertyAssertionAxioms) {
            negativeObjectPropertyAssertionAxiom.getObjectPropertiesInSignature().forEach(objectProperty -> {
                objectPropertyAssertions.add("¬" + objectProperty.getIRI().getFragment());
            });
        }

        return StringUtils.join(objectPropertyAssertions, " ");
    }
}
