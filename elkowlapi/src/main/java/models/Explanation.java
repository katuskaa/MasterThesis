package models;

import common.Printer;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Explanation {

    private Collection<OWLAxiom> owlAxioms;

    private Integer depth;

    public Explanation(Collection<OWLAxiom> owlAxioms) {
        this.owlAxioms = owlAxioms;
    }

    public Explanation() {
        this.owlAxioms = new LinkedList<>();
    }

    public Collection<OWLAxiom> getOwlAxioms() {
        return owlAxioms;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public void addAxioms(Collection<OWLAxiom> axioms) {
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

    @Override
    public int hashCode() {
        return owlAxioms.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Explanation) {
            Explanation exp = (Explanation) obj;
            return exp.getOwlAxioms().equals(owlAxioms);
        }

        return false;
    }
}
