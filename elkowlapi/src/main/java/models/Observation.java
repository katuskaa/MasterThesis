package models;

import common.Configuration;
import common.Printer;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.LinkedList;
import java.util.List;

public class Observation {

    private List<OWLAxiom> axioms;

    public Observation() {
        this.axioms = new LinkedList<>();
    }

    public List<OWLAxiom> getOwlAxioms() {
        return axioms;
    }

    public void addOwlAxiom(OWLAxiom axiom) {
        axioms.add(axiom);
    }

    @Override
    public String toString() {
        List<String> observation = new LinkedList<>();

        for (OWLAxiom axiom : axioms) {
            observation.add(Printer.print(axiom));
        }

        return StringUtils.join(observation, Configuration.DELIMITER_OBSERVATION);
    }

}
