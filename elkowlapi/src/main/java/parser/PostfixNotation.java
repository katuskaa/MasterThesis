package parser;

import common.DLSyntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class PostfixNotation {

    private final List<String> postfixExpression;
    private String[][] operatorsByPrecedence = {{DLSyntax.CONJUNCTION, DLSyntax.DISJUNCTION}, {DLSyntax.EXISTS, DLSyntax.FOR_ALL}};

    PostfixNotation(String expression) {
        List<String> tokens = convertToTokens(expression);
        postfixExpression = convertToPostfixNotation(tokens);
    }

    List<String> getPostfixExpression() {
        return postfixExpression;
    }

    private List<String> convertToTokens(String expression) {
        String[] possibleTokens = expression.split(DLSyntax.DELIMITER_EXPRESSION);
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i < possibleTokens.length; i++) {
            if (possibleTokens[i].startsWith(DLSyntax.LEFT_PARENTHESES)) {
                tokens.add(DLSyntax.LEFT_PARENTHESES);
                tokens.add(possibleTokens[i].substring(1));

            } else if (possibleTokens[i].endsWith(DLSyntax.RIGHT_PARENTHESES)) {
                tokens.add(possibleTokens[i].substring(0, possibleTokens[i].length() - 1));
                tokens.add(DLSyntax.RIGHT_PARENTHESES);

            } else if (possibleTokens[i].equals(DLSyntax.NOMINAL) || possibleTokens[i].equals(DLSyntax.NEGATION)) {
                if (i + 1 < possibleTokens.length) {
                    String actualToken = possibleTokens[i];
                    String nextToken = possibleTokens[++i];

                    if (nextToken.endsWith(DLSyntax.RIGHT_PARENTHESES)) {
                        String validNextToken = nextToken.substring(0, nextToken.length() - 1);
                        String token = actualToken.concat(DLSyntax.DELIMITER_EXPRESSION).concat(validNextToken);

                        tokens.add(token);
                        tokens.add(DLSyntax.RIGHT_PARENTHESES);

                    } else {
                        String token = actualToken.concat(DLSyntax.DELIMITER_EXPRESSION).concat(nextToken);
                        tokens.add(token);
                    }

                } else {
                    throw new RuntimeException("Manchester syntax forces you to have after keyword value a nominal!");
                }

            } else {
                tokens.add(possibleTokens[i]);
            }
        }

        return tokens;
    }

    private List<String> convertToPostfixNotation(List<String> tokens) {
        Stack<String> operators = new Stack<>();
        List<String> postfix = new ArrayList<>();

        for (String token : tokens) {
            if (token.equals(DLSyntax.LEFT_PARENTHESES)) {
                operators.push(token);
            } else if (token.equals(DLSyntax.RIGHT_PARENTHESES)) {
                while (!operators.peek().equals(DLSyntax.LEFT_PARENTHESES)) {
                    postfix.add(operators.pop());
                }

                operators.pop();

            } else if (!isOperator(token)) {
                postfix.add(token);

            } else {
                boolean tokenProcessed = false;

                while (!tokenProcessed) {
                    if (operators.isEmpty() || operators.peek().equals(DLSyntax.LEFT_PARENTHESES)) {
                        operators.push(token);
                        tokenProcessed = true;
                    } else {
                        String topOperator = operators.peek();

                        if (getPrecedence(token) > getPrecedence(topOperator) || getPrecedence(token) == getPrecedence(topOperator)) {
                            operators.push(token);
                            tokenProcessed = true;
                        } else {
                            postfix.add(operators.pop());
                        }
                    }
                }
            }
        }

        while (!operators.isEmpty()) {
            postfix.add(operators.pop());
        }

        return postfix;
    }

    private boolean isOperator(String token) {
        for (String[] operators : operatorsByPrecedence) {
            for (String operator : operators) {
                if (token.equals(operator)) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getPrecedence(String operator) {
        for (int i = 0; i < operatorsByPrecedence.length; i++) {
            for (int j = 0; j < operatorsByPrecedence[i].length; j++) {
                if (operator.equals(operatorsByPrecedence[i][j])) {
                    return i;
                }
            }
        }

        throw new RuntimeException("Invalid operator specified (" + operator + ")");
    }

}
