package ru.beeline.interpreter.ast;

import ru.beeline.interpreter.ast.interfaces.ExpressionInterface;
import ru.beeline.interpreter.token.Token;

/**
 * Holds the identifier of the binding (the x in let x = 5;).
 * Although it seems like identifiers can't produce the value (especially)
 * in 'let' statements, but this class implements ExpressionInterface because
 * that identifiers in other parts of BeeLang DO produce values: let x = valueProducingIdentifier.
 * <p>Thus, to keep the number of different node types small, we'll use this class to represent
 * the name in a variable binding and reuse it to represent an identifier as a part of or a complete
 * expression.</p>
 */
public class Identifier implements ExpressionInterface
{
    public Token token;    // the IDENT token. To keep track of the token the AST node is associated with
    public String value;

    public Identifier(Token token, String value)
    {
        this.token = token;
        this.value = value;
    }

    @Override
    public String getTokenLiteral()
    {
        return token.literal;
    }

    @Override
    public void expressionNode() { }
    
}
