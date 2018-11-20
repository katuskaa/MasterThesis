package models;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public class KnowledgeBase {

    private Set<OWLAxiom> TBoxAxioms;
    private Set<OWLAxiom> ABoxAxioms;

    public KnowledgeBase(Set<OWLAxiom> TBoxAxioms, Set<OWLAxiom> ABoxAxioms) {
        this.TBoxAxioms = TBoxAxioms;
        this.ABoxAxioms = ABoxAxioms;
    }

    public Set<OWLAxiom> getTBoxAxioms() {
        return TBoxAxioms;
    }

    public Set<OWLAxiom> getABoxAxioms() {
        return ABoxAxioms;
    }

}
