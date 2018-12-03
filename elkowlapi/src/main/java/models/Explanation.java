package models;

import common.Printer;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public void addAxioms(Set<OWLAxiom> axioms) {
        this.owlAxioms.addAll(axioms);
    }

    public void addAxiom(OWLAxiom axiom) {
        this.owlAxioms.add(axiom);
    }

    @Override
    public String toString() {
        List<String> result = new ArrayList<>();

        for (OWLAxiom owlAxiom : owlAxioms) {
            result.add(Printer.print(owlAxiom));
        }

        return "{ " + StringUtils.join(result, ",") + " }";
    }
}
