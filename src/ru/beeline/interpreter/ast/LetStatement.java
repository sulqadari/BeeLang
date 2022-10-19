package ru.beeline.interpreter.ast;

import ru.beeline.interpreter.token.Token;

public class LetStatement implements StatementInterface
{
    Token token;                // to keep track of the token the AST node is associated with
    Identifier name;            // holds the identifier of the binding
    ExpressionInterface value;  // holds the expression that produces the value

    @Override
    public String tokenLiteral()
    {
        return token.literal;
    }

    @Override
    public void statementNode(){ }
}
