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
            parseAssertion(observation.split(DLSyntax.DELIMITER_ASSERTION));
        }
    }

    private void parseAssertion(String[] expressions) {
        OWLNamedIndividual namedIndividual = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(expressions[0])));

        loader.addNamedIndividual(namedIndividual);
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(namedIndividual));

        PostfixNotation postfixNotation = new PostfixNotation(expressions[1]);
        OWLExpression expression = parseExpression(postfixNotation.getPostfixExpression());

        switch (expression.typ) {
            case CLASS_EXPRESSION:
                loader.setObservation(loader.getDataFactory().getOWLClassAssertionAxiom(expression.classExpression, namedIndividual));
                loader.setNegObservation(loader.getDataFactory().getOWLClassAssertionAxiom(expression.classExpression.getComplementNNF(), namedIndividual));
                break;

            case NEGATIVE_OBJECT_PROPERTY_ASSERTION:
                loader.setObservation(expression.negativeObjectPropertyAssertionAxiom);
                loader.setNegObservation(expression.objectPropertyAssertionAxiom);
                break;

            case OBJECT_PROPERTY_ASSERTION:
                loader.setObservation(expression.objectPropertyAssertionAxiom);
                loader.setNegObservation(expression.negativeObjectPropertyAssertionAxiom);
                break;

            default:
                break;
        }


        //TODO test if nominal needs to be added to ontology as individual if it is not already in
    }

    private OWLExpression parseExpression(List<String> postfixExpression) {
        Stack<OWLExpression> stack = new Stack<>();

        if (postfixExpression.size() == 1) {
            OWLExpression expression = new OWLExpression();

            expression.classExpression = createClassExpression(postfixExpression.get(0));
            expression.typ = OWLTyp.CLASS_EXPRESSION;

            return expression;
        } else if (postfixExpression.size() == 3 && (postfixExpression.get(2).equals(DLSyntax.EXISTS) || postfixExpression.get(2).equals(DLSyntax.FOR_ALL))) {

            OWLExpression expression = new OWLExpression();

            String left = postfixExpression.get(0);
            String right = postfixExpression.get(1);

            if (containsNegation(left)) {
                expression.negativeObjectPropertyAssertionAxiom = createNegativeObjectPropertyAssertionAxiom(left, right);
                expression.objectPropertyAssertionAxiom = createObjectPropertyAssertionAxiom(left, right);
                expression.typ = OWLTyp.NEGATIVE_OBJECT_PROPERTY_ASSERTION;
            } else {
                expression.objectPropertyAssertionAxiom = createObjectPropertyAssertionAxiom(left, right);
                expression.negativeObjectPropertyAssertionAxiom = createNegativeObjectPropertyAssertionAxiom(left, right);
                expression.typ = OWLTyp.OBJECT_PROPERTY_ASSERTION;
            }

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
        OWLClassExpression classExpression;

        if (containsNegation(name)) {
            String className = name.split(DLSyntax.DELIMITER_EXPRESSION)[1];
            classExpression = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(className))).getComplementNNF();
        } else {
            classExpression = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));
        }

        return classExpression;
    }

    private OWLObjectProperty createObjectPropertyExpression(String name) {
        OWLObjectProperty objectProperty;

        if (containsNegation(name)) {
            String objectPropertyName = name.split(DLSyntax.DELIMITER_EXPRESSION)[1];
            objectProperty = loader.getDataFactory().getOWLObjectProperty(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(objectPropertyName)));
        } else {
            objectProperty = loader.getDataFactory().getOWLObjectProperty(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));
        }

        return objectProperty;
    }

    private OWLNamedIndividual createNamedIndividual(String name) {
        return loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));
    }

    private OWLObjectPropertyAssertionAxiom createObjectPropertyAssertionAxiom(String left, String right) {
        OWLObjectProperty objectProperty = createObjectPropertyExpression(left);

        OWLNamedIndividual subject = loader.getIndividuals().getNamedIndividuals().get(0);
        OWLNamedIndividual object = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(right)));

        loader.addNamedIndividual(object);
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(object));

        return loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object);
    }

    private OWLNegativeObjectPropertyAssertionAxiom createNegativeObjectPropertyAssertionAxiom(String left, String right) {
        OWLObjectProperty objectProperty = createObjectPropertyExpression(left);

        OWLNamedIndividual subject = loader.getIndividuals().getNamedIndividuals().get(0);
        OWLNamedIndividual object = loader.getDataFactory().getOWLNamedIndividual(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(right)));

        loader.addNamedIndividual(object);
        loader.getOntologyManager().addAxiom(loader.getOntology(), loader.getDataFactory().getOWLDeclarationAxiom(object));

        return loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object);
    }

    private boolean containsNegation(String concept) {
        return concept.contains(DLSyntax.NEGATION);
    }

    private boolean isNominal(String nominal) {
        return nominal.startsWith(DLSyntax.NOMINAL);
    }

}
