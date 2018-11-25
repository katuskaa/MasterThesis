package models;

import common.Printer;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

public class Explanation {

    private Set<OWLAxiom> owlAxioms;

    public Explanation(Set<OWLAxiom> owlAxioms) {
        this.owlAxioms = owlAxioms;
    }

    public Explanation() {
        this.owlAxioms = new HashSet<>();
    }

    public Set<OWLAxiom> getOwlAxioms() {
        return owlAxioms;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (OWLAxiom owlAxiom : owlAxioms) {
            result.append(Printer.print(owlAxiom)).append(";");
        }

        return result.toString();
    }
}
