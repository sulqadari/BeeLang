package ru.beeline.interpreter.ast;

import ru.beeline.interpreter.ast.interfaces.ExpressionInterface;
import ru.beeline.interpreter.ast.interfaces.StatementInterface;
import ru.beeline.interpreter.token.Token;

/**
 * 
 */
public class LetStatement implements StatementInterface
{
    public Token token;                // the LET token. To keep track of the token the AST node is associated with
    public Identifier name;            // holds the identifier of the binding
    public ExpressionInterface value;  // holds the expression that produces the value
    
    public LetStatement(Token token)
    {
        this.token = token;
    }

    @Override
    public String getTokenLiteral()
    {
        return token.literal;
    }

    @Override
    public void statementNode(){ }
}
