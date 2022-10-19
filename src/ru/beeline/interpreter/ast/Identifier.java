package ru.beeline.interpreter.ast;

import ru.beeline.interpreter.token.Token;

/**
 * To hold the identifier of the binding, the x in let x = 5;, we have the Identifier struct type,
 * which implements the Expression interface.
 */
public class Identifier implements ExpressionInterface
{
    Token token;
    String value;

    @Override
    public String tokenLiteral()
    {
        return token.literal;
    }

    @Override
    public void expressionNode() { }
    
}
