package ru.beeline.interpreter.ast;

import ru.beeline.interpreter.ast.interfaces.ExpressionInterface;
import ru.beeline.interpreter.ast.interfaces.StatementInterface;
import ru.beeline.interpreter.token.Token;

public class ReturnStatement  implements StatementInterface
{
    public Token token;                 // the 'return' token
    public ExpressionInterface value;

    public ReturnStatement(Token token)
    {
        this.token = token;
    }

    @Override
    public String getTokenLiteral()
    {
        return token.literal;
    }

    @Override
    public void statementNode() { }
    
}
