package models;

import common.Printer;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.List;

public class Literals {

    private List<OWLAxiom> owlAxioms;

    public Literals() {
        this.owlAxioms = new ArrayList<>();
    }

    public Literals(List<OWLAxiom> owlAxioms) {
        this.owlAxioms = owlAxioms;
    }

    public List<OWLAxiom> getOwlAxioms() {
        return owlAxioms;
    }

    public void addLiterals(Literals literals) {
        this.owlAxioms.addAll(literals.getOwlAxioms());
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
