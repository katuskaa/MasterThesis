package models;

import common.Printer;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public class Explanation {

    private Set<OWLAxiom> owlAxioms;

    public Explanation(Set<OWLAxiom> owlAxioms) {
        this.owlAxioms = owlAxioms;
    }

    public Set<OWLAxiom> getOwlAxioms() {
        return owlAxioms;
    }

    public void setOwlAxioms(Set<OWLAxiom> owlAxiom) {
        this.owlAxioms = owlAxioms;
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
