package ru.beeline.interpreter.ast;

public class RootNode implements NodeInterface
{
    StatementInterface[] statements;

    public RootNode(StatementInterface[] statements)
    {
        this.statements = statements;
    }

    @Override
    public String tokenLiteral()
    {
        if (statements.length > 0)
            return statements[0].tokenLiteral();
        else
            return "";
    }
    
}
