package common;

import com.sun.deploy.util.StringUtils;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Printer {

    public static String print(OWLAxiom owlAxiom) {
        return Printer.getNamedIndividual(owlAxiom) + ":" + Printer.getAssertionAxiom(owlAxiom);
    }

    private static String getNamedIndividual(OWLAxiom owlAxiom) {
        List<String> owlNamedIndividuals = new ArrayList<>();
        Set<OWLNamedIndividual> individualsInSignature = owlAxiom.getIndividualsInSignature();
        for (OWLNamedIndividual owlNamedIndividual : individualsInSignature) {
            owlNamedIndividuals.add(owlNamedIndividual.getIRI().getFragment());
        }
        return StringUtils.join(owlNamedIndividuals, ",");
    }

    private static String getAssertionAxiom(OWLAxiom owlAxiom) {
        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(owlAxiom);

        Set<OWLAxiom> assertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.CLASS_ASSERTION);
        List<String> assertions = new ArrayList<>();

        for (OWLAxiom assertionAxiom : assertionAxioms) {
            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) assertionAxiom).getClassExpression();

            if (owlClassExpression instanceof OWLClass) {
                assertions.add(owlClassExpression.asOWLClass().getIRI().getFragment());
            } else if (owlClassExpression instanceof OWLObjectComplementOf) {
                assertions.add("¬" + owlClassExpression.getComplementNNF().asOWLClass().getIRI().getFragment());
            }
        }

        return StringUtils.join(assertions, " ");
    }
}
