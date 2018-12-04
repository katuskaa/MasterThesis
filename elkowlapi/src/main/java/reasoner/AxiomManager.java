package reasoner;

import common.Configuration;
import org.semanticweb.owlapi.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AxiomManager {

    public static List<OWLAxiom> createClassAssertionAxiom(ILoader loader, OWLAxiom axiom) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (OWLDeclarationAxiom.class.isAssignableFrom(axiom.getClass())) {
            String name = ((OWLDeclarationAxiom) axiom).getEntity().getIRI().getFragment();

            for (OWLNamedIndividual namedIndividual : loader.getIndividuals().getNamedIndividuals()) {
                if (name.equals(namedIndividual.getIRI().getFragment())) {
                    return owlAxioms;
                }
            }

            OWLClass owlClass = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(Configuration.DELIMITER_ONTOLOGY).concat(name)));

            for (OWLNamedIndividual namedIndividual : loader.getIndividuals().getNamedIndividuals()) {
                owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
            }
        }

        return owlAxioms;
    }

    public static List<OWLAxiom> createObjectPropertyAssertionAxiom(ILoader loader, OWLAxiom axiom) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        return owlAxioms;
    }

    //TODO add object property negation
    public static OWLAxiom getComplementOfOWLAxiom(ILoader loader, OWLAxiom owlAxiom) {
        Set<OWLClass> names = owlAxiom.getClassesInSignature();
        String name = "";
        OWLAxiom complement = null;

        if (names != null && names.size() == 1) {
            for (OWLClass owlClass : names) {
                name = owlClass.getIRI().getFragment();
            }

            OWLNamedIndividual namedIndividual = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(Configuration.DELIMITER_ONTOLOGY).concat(Configuration.INDIVIDUAL)));
            OWLClass owlClass = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(Configuration.DELIMITER_ONTOLOGY).concat(name)));

            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) owlAxiom).getClassExpression();

            if (OWLObjectComplementOf.class.isAssignableFrom(owlClassExpression.getClass())) {
                complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual);
            } else {
                complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual);
            }
        } else {
            System.out.println("names wrong count!");
        }

        return complement;
    }


}
