package models;

import common.Printer;
import org.semanticweb.owlapi.model.OWLAxiom;

public class Observation {

    private OWLAxiom owlAxiom;

    public Observation(OWLAxiom owlAxiom) {
        this.owlAxiom = owlAxiom;
    }

    public OWLAxiom getOwlAxiom() {
        return owlAxiom;
    }

    public void setOwlAxiom(OWLAxiom owlAxiom) {
        this.owlAxiom = owlAxiom;
    }

    @Override
    public String toString() {
        return Printer.print(owlAxiom);
    }

}
