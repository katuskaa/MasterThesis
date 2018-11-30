package reasoner;

import common.Configuration;
import org.semanticweb.owlapi.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AxiomManager {

    public static List<OWLAxiom> createClassAssertionAxiom(ILoader loader, OWLAxiom owlAxiom) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
        OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();

        if (OWLDeclarationAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
            String name = ((OWLDeclarationAxiom) owlAxiom).getEntity().getIRI().getFragment();

            if (name.equals(Configuration.INDIVIDUAL)) {
                return owlAxioms;
            }

            OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(Configuration.INDIVIDUAL)));
            OWLClass owlClass = dataFactory.getOWLClass(IRI.create(ontologyIRI.concat("#").concat(name)));

            owlAxioms.add(dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual));
            owlAxioms.add(dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
        } else {
            System.out.println("Wrong axiom format, expected OWLDeclarationAxiom!");
        }

        return owlAxioms;
    }

    public static void createObjectPropertyAssertionAxiom(OWLAxiom axiom) {
        //TODO finish
    }

    public static OWLAxiom getComplementOfOWLAxiom(ILoader loader, OWLAxiom owlAxiom) {
        String ontologyIRI = loader.getOntology().getOntologyID().getOntologyIRI().toString();
        OWLDataFactory dataFactory = loader.getOntologyManager().getOWLDataFactory();

        Set<OWLClass> names = owlAxiom.getClassesInSignature();
        String name = "";
        OWLAxiom complement = null;

        if (names != null && names.size() == 1) {
            for (OWLClass owlClass : names) {
                name = owlClass.getIRI().getFragment();
            }

            OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.concat("#").concat(Configuration.INDIVIDUAL)));
            OWLClass owlClass = dataFactory.getOWLClass(IRI.create(ontologyIRI.concat("#").concat(name)));

            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) owlAxiom).getClassExpression();

            if (OWLObjectComplementOf.class.isAssignableFrom(owlClassExpression.getClass())) {
                complement = dataFactory.getOWLClassAssertionAxiom(owlClass, namedIndividual);
            } else {
                complement = dataFactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual);
            }
        } else {
            System.out.println("names wrong count!");
        }

        return complement;
    }


}
