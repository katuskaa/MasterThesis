package common;

import org.semanticweb.owlapi.model.*;
import reasoner.Loader;

import java.util.*;


public class ObservationParser implements IObservationParser {

    private Loader loader;

    public ObservationParser(Loader loader) {
        this.loader = loader;
    }

    @Override
    public void parse() {
        String[] observations;

        if (Configuration.MULTI_OBSERVATION) {
            observations = Configuration.OBSERVATION.split(Configuration.DELIMITER_OBSERVATION);
        } else {
            observations = new String[1];
            observations[0] = Configuration.OBSERVATION;
        }

        for (String observation : observations) {
            String[] expressions = observation.split(DLSyntax.DELIMITER_ASSERTION);

            if (expressions[0].contains(DLSyntax.DELIMITER_OBJECT_PROPERTY)) {
                parseObjectProperty(expressions);
            } else {
                parseClassAssertion(expressions);
            }
        }
    }

    private void parseClassAssertion(String[] expressions) {
        OWLNamedIndividual namedIndividual = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(expressions[0])));

        PostfixNotation postfixNotation = new PostfixNotation(expressions[1]);
        OWLExpression expression = parseExpression(postfixNotation.getPostfixExpression());

        loader.addNamedIndividual(namedIndividual);
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(namedIndividual));

        loader.setObservation(loader.getDataFactory().getOWLClassAssertionAxiom(expression.classExpression, namedIndividual));
        loader.setNegObservation(loader.getDataFactory().getOWLClassAssertionAxiom(expression.classExpression.getComplementNNF(), namedIndividual));

        //TODO test if nominal needs to be added to ontology as individual if it is not already in
    }

    private void parseObjectProperty(String[] expressions) {
        String[] individuals = expressions[0].split(DLSyntax.DELIMITER_OBJECT_PROPERTY);

        OWLNamedIndividual subject = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(individuals[0])));
        OWLNamedIndividual object = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(individuals[1])));
        OWLObjectProperty objectProperty = loader.getDataFactory().getOWLObjectProperty(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(expressions[1])));

        List<OWLNamedIndividual> alreadyInOntology = new ArrayList<>();

        loader.getOntology().axioms(AxiomType.DECLARATION).forEach(axiom -> {
            if (OWLNamedIndividual.class.isAssignableFrom(axiom.getEntity().getClass())) {
                alreadyInOntology.add((OWLNamedIndividual) axiom.getEntity());
            }
        });

        if (!alreadyInOntology.contains(subject)) {
            loader.addNamedIndividual(subject);
        }

        if (!alreadyInOntology.contains(object)) {
            loader.addNamedIndividual(object);
        }

        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(subject));
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(object));

        loader.setObservation(loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
        loader.setNegObservation(loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
    }

    private OWLExpression parseExpression(List<String> postfixExpression) {
        Stack<OWLExpression> stack = new Stack<>();

        if (postfixExpression.size() == 1) {
            OWLExpression expression = new OWLExpression();

            expression.classExpression = createClassExpression(postfixExpression.get(0));
            expression.typ = OWLTyp.CLASS_EXPRESSION;

            return expression;
        }

        for (String token : postfixExpression) {
            if (!isOperator(token)) {
                OWLExpression expression = new OWLExpression();

                if (isNominal(token)) {
                    String nominal = token.split(DLSyntax.DELIMITER_EXPRESSION)[1];
                    OWLNamedIndividual namedIndividual = createNamedIndividual(nominal);
                    OWLObjectOneOf objectOneOf = loader.getDataFactory().getOWLObjectOneOf(namedIndividual);
                    expression.classExpression = objectOneOf.asObjectUnionOf();
                    expression.typ = OWLTyp.CLASS_EXPRESSION;
                } else {

                    expression.token = token;
                    expression.typ = OWLTyp.TOKEN_NOT_DEFINED;
                }

                stack.push(expression);

            } else {
                OWLExpression right = stack.pop();
                OWLExpression left = stack.pop();

                if (right.typ == OWLTyp.TOKEN_NOT_DEFINED) {
                    right.classExpression = createClassExpression(right.token);
                    right.token = null;
                    right.typ = OWLTyp.CLASS_EXPRESSION;
                }

                switch (token) {
                    case DLSyntax.CONJUNCTION:
                        OWLExpression intersection = new OWLExpression();

                        if (left.typ == OWLTyp.TOKEN_NOT_DEFINED) {
                            left.classExpression = createClassExpression(left.token);
                            left.token = null;
                            left.typ = OWLTyp.CLASS_EXPRESSION;
                        }

                        intersection.classExpression = createIntersectionOf(right, left);
                        intersection.typ = OWLTyp.CLASS_EXPRESSION;

                        stack.push(intersection);
                        break;

                    case DLSyntax.DISJUNCTION:
                        OWLExpression union = new OWLExpression();

                        if (left.typ == OWLTyp.TOKEN_NOT_DEFINED) {
                            left.classExpression = createClassExpression(left.token);
                            left.token = null;
                            left.typ = OWLTyp.CLASS_EXPRESSION;
                        }

                        union.classExpression = createUnionOf(right, left);
                        union.typ = OWLTyp.CLASS_EXPRESSION;

                        stack.push(union);
                        break;

                    case DLSyntax.EXISTS:
                        left.objectProperty = createObjectPropertyExpression(left.token);
                        left.token = null;
                        left.typ = OWLTyp.OBJECT_PROPERTY;

                        OWLObjectSomeValuesFrom objectSomeValuesFrom = createExistentialRestriction(right, left);
                        OWLExpression someValues = new OWLExpression();

                        someValues.classExpression = objectSomeValuesFrom;
                        someValues.typ = OWLTyp.CLASS_EXPRESSION;

                        stack.push(someValues);
                        break;

                    case DLSyntax.FOR_ALL:
                        left.objectProperty = createObjectPropertyExpression(left.token);
                        left.token = null;
                        left.typ = OWLTyp.OBJECT_PROPERTY;

                        OWLObjectAllValuesFrom objectAllValuesFrom = createValueRestriction(right, left);
                        OWLExpression allValues = new OWLExpression();

                        allValues.classExpression = objectAllValuesFrom;
                        allValues.typ = OWLTyp.CLASS_EXPRESSION;

                        stack.push(allValues);
                        break;

                    default:
                        throw new RuntimeException("Invalid operator ( " + token + " )");
                }
            }
        }

        return stack.pop();
    }

    private boolean isOperator(String token) {
        return token.equals(DLSyntax.CONJUNCTION) || token.equals(DLSyntax.DISJUNCTION) || token.equals(DLSyntax.EXISTS) || token.equals(DLSyntax.FOR_ALL);
    }

    private OWLObjectIntersectionOf createIntersectionOf(OWLExpression right, OWLExpression left) {
        Set<OWLClassExpression> intersectionOfConcepts = new HashSet<>();

        List<OWLExpression> expressions = new ArrayList<>();
        expressions.add(right);
        expressions.add(left);

        for (OWLExpression expression : expressions) {
            if (expression.typ == OWLTyp.CLASS_EXPRESSION) {
                intersectionOfConcepts.add(expression.classExpression);
            } else {
                throw new RuntimeException("Intersection: Wrong type: typ = " + expression.typ.toString());
            }
        }

        return loader.getDataFactory().getOWLObjectIntersectionOf(intersectionOfConcepts);
    }

    private OWLObjectUnionOf createUnionOf(OWLExpression right, OWLExpression left) {
        Set<OWLClassExpression> unionOfConcepts = new HashSet<>();

        List<OWLExpression> expressions = new ArrayList<>();
        expressions.add(right);
        expressions.add(left);

        for (OWLExpression expression : expressions) {
            if (expression.typ == OWLTyp.CLASS_EXPRESSION) {
                unionOfConcepts.add(expression.classExpression);
            } else {
                throw new RuntimeException("Union: Wrong type: typ = " + expression.typ.toString());
            }
        }

        return loader.getDataFactory().getOWLObjectUnionOf(unionOfConcepts);
    }

    private OWLObjectSomeValuesFrom createExistentialRestriction(OWLExpression right, OWLExpression left) {
        if (right.typ == OWLTyp.CLASS_EXPRESSION) {
            return loader.getDataFactory().getOWLObjectSomeValuesFrom(left.objectProperty, right.classExpression);
        }

        throw new RuntimeException("SomeValuesFrom: Wrong right expression: typ = " + right.typ.toString());
    }

    private OWLObjectAllValuesFrom createValueRestriction(OWLExpression right, OWLExpression left) {
        if (right.typ == OWLTyp.CLASS_EXPRESSION) {
            return loader.getDataFactory().getOWLObjectAllValuesFrom(left.objectProperty, right.classExpression);
        }

        throw new RuntimeException("All values from: Wrong right expression: typ = " + right.typ.toString());
    }

    private OWLClassExpression createClassExpression(String name) {
        OWLClassExpression classExpression = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));

        if (containsNegation(name)) {
            classExpression = classExpression.getComplementNNF();
        }

        return classExpression;
    }

    private OWLObjectProperty createObjectPropertyExpression(String name) {
        return loader.getDataFactory().getOWLObjectProperty(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));
    }

    private OWLNamedIndividual createNamedIndividual(String name) {
        return loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));
    }

    private boolean containsNegation(String concept) {
        return concept.contains(DLSyntax.NEGATION);
    }

    private boolean isNominal(String nominal) {
        return nominal.startsWith(DLSyntax.NOMINAL);
    }

}
