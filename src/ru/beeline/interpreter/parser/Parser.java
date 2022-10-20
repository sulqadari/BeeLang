package ru.beeline.interpreter.parser;

import java.util.ArrayList;

import ru.beeline.interpreter.ast.Identifier;
import ru.beeline.interpreter.ast.LetStatement;
import ru.beeline.interpreter.ast.ReturnStatement;
import ru.beeline.interpreter.ast.RootNode;
import ru.beeline.interpreter.ast.interfaces.StatementInterface;
import ru.beeline.interpreter.lexer.Lexer;
import ru.beeline.interpreter.token.Token;

public class Parser
{
    /** Repeatedly calls nextToken() to get the next token in input */
    Lexer lexer;
    Token currToken;
    Token nextToken;
    ArrayList<String> errors;

    public Parser(Lexer lexer)
    {
        this.lexer = lexer;
        currToken = null;
        nextToken = null;

        errors = new ArrayList<>();

        nextToken();
        nextToken();
    }

    public RootNode parse()
    {
        RootNode root = new RootNode(new ArrayList<>());
        StatementInterface statement;

        while(!currTokenIs(Token.EOF))
        {
            statement = parseStatement();

            if (statement != null)
                root.statements.add(statement);
            
            nextToken();
        }
        return root;
    }

    public ArrayList<String> getErrorsList()
    {
        return errors;
    }

    private StatementInterface parseStatement()
    {
        switch(currToken.type)
        {
            case Token.LET:
                return parseLetStatement();

            case Token.RETURN:
                return parseReturnStatement();
            
            default:
                return null;
        }
    }

    private LetStatement parseLetStatement()
    {
        LetStatement letStatement = new LetStatement(currToken);
        
        //return NULL if next token is not of type IDENT
        if(!expectedNextToken(Token.IDENT))
            return null;
        
        letStatement.name = new Identifier(currToken, currToken.literal);
        //return NULL if the token following IDENT is not of type ASSIGN
        if(!expectedNextToken(Token.ASSIGN))
            return null;
        
        // TODO: We're skipping the expressions until we encounter a semicolon
        while(!currTokenIs(Token.SEMICOLON))
        {
            nextToken();
        }

        return letStatement;
    }

    private ReturnStatement parseReturnStatement()
    {
        ReturnStatement returnStatement = new ReturnStatement(currToken);
        nextToken();

        // TODO: We're skipping the expressions until we encounter a semicolon
        while(!currTokenIs(Token.SEMICOLON))
        {
            nextToken();
        }
        return returnStatement;
    }

    /**
     * <p>Assertion method.</p>
     * Its primary purpose is to enforce the correctness of the order
     * of tokens by checking the type of the next token.
     * <p>It checks the type of the <code>nextToken</code> and only if the type
     * is correct does it advance the tokens by calling <code>nextToken()<code></p>
     * @param type
     * @return
     */
    private boolean expectedNextToken(String type)
    {
        if(nextTokenIs(type))
        {
            nextToken();
            return true;
        }else
        {
            peekError(type);
            return false;
        }
    }

    private boolean nextTokenIs(String type)
    {
        return nextToken.type == type;
    }

    private boolean currTokenIs(String type)
    {
        return currToken.type == type;
    }

    private void nextToken()
    {
        currToken = nextToken;
        nextToken = lexer.nextToken();
    }

    private void peekError(String type)
    {
        String message = String.format("Expected next token to be '%s', got '%s' instead.", type, nextToken.type);
        errors.add(message);
    }

}
