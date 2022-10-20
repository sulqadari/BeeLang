package ru.beeline.interpreter.ast;

import java.util.ArrayList;

import ru.beeline.interpreter.ast.interfaces.NodeInterface;
import ru.beeline.interpreter.ast.interfaces.StatementInterface;

/**
 * The root node of every AST our parser produces.
 */
public class RootNode implements NodeInterface
{
    /** Every valid Monkey program is a series of statements. */
    public ArrayList <StatementInterface> statements;

    public RootNode(ArrayList <StatementInterface> statements)
    {
        this.statements = statements;
    }

    @Override
    public String getTokenLiteral()
    {
        if (statements.size() > 0)  //fetch root statement
            return statements.get(0).getTokenLiteral();
        else                        //return nothing
            return "";
    }
    

}
