package ru.beelang;

import java.util.ArrayList;
import java.util.List;
import static ru.beelang.TokenType.*;

/**
 * The parser consumes a flat input sequence of tokens<p/>
 * Each grammar rule becomes a method inside this class.
 * Each method for parsing a grammar rule produces a syntax
 * tree for that rule and returns it to the caller.<p/>
 * When the body of the rule contains a nonterminal - a reference
 * to another rule - we call that other rule's method.
 * <p>Precedence table (priority in descendant order)<p/>
 * <ul>
 * <li>expression -> <code>equality;</code></li>
 * <li>equality -> <code>comparison ( ( "!=" | "==" ) comparison )*;</code></li>
 * <li>comparison -> <code>term ( ( ">" | ">=" | "<" | "<=" ) term )*;</code></li>
 * <li>term -> <code>factor ( ( "-" | "+" ) factor )*;</code></li>
 * <li>factor -> <code>unary ( ( "/" | "*" ) unary )*;</code></li>
 * <li>unary -> <code>( "!" | "-" ) unary | primary;</code></li>
 * <li>primary -> <code>NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")";</code></li>
 * </ul>
 * <p>A recursive descent parser is a literal translation of the grammar's rules straight into imperative code.
 * Each rule becomes a function. The body of the rule translates to code roughly like:</p>
 * 
 * <ul>
 * <li>Terminal: <code>Code to match and consume a token</code></li>
 * <li>Nonterminal: <code>Call to that rule's function</code></li>
 * <li>| :<code>if or switch statement</code></li>
 * <li>* or + :<code>loop (while or for)</code></li>
 * <li>? :<code>if statement</code></li>
 * </ul>
 */
public class Parser
{
    private static class ParseError extends RuntimeException {}

    /** Stores the tokens */
    private final List<Token> tokens;
    /** Points to the next token to be parsed */
    private int current = 0;

    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    List<Stmt> parse()
    {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd())
        {
            statements.add(declaration());
        }

        return statements;
    }

    /**
     * Parses a statements.
     * @return
     */
    private Stmt statement()
    {
        if (match(PRINT))
            return printStatement();
        
        if (match(LEFT_BRACE))
            return new Stmt.Block(block());
        
        return expressionStatement();
    }

    private Stmt printStatement()
    {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value");

        return new Stmt.Print(value);
    }

    private Stmt varDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL))
        {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement()
    {
        Expr expr = expression();

        consume(SEMICOLON, "Expect ';' after expression");

        return new Stmt.Expression(expr);
    }

    /**
     *  Parses statements and add them to the list until we reach the end of the block
     * marked by the closing curly bracket.
     * @return
     */
    private List<Stmt> block()
    {
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd())
            statements.add(declaration());
        
        consume(RIGHT_BRACE, "Expect '}' after block");
        return statements;
    }
    /**
     * Handles assignment expressions (l-value statements).<p/>
     * The trick is that right before we create the assignment expression node,
     * we look at the left-hand side expression and figure out what kind of assignment
     * target it is. We convert the r-value expression node into an l-value representation.
     * 
     * @return
     */
    private Expr assignment()
    {
        Expr expr = equality();

        // parse right-hand side
        if(match(EQUAL))
        {
            Token equals = previous();
            //Since assignment is right-associative, we recursively call
            // assignment() to parse the right-hand side.
            Expr value = assignment();

            if (expr instanceof Expr.Variable)
            {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            //report an error if left-hand side isn't a valid assignment target.
            //
            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    /**
     * Expression grammar<p/>
     * The rule for expression is as follow:<p/>
     * <code>expression -> equality;</code>
     * @return
     */
    private Expr expression()
    {
        //return equality();
        return assignment();
    }

    private Stmt declaration()
    {
        try
        {
            if (match(VAR))
                return varDeclaration();
            
            return statement();
        }catch(ParseError error)
        {
            synchronize();
            return null;
        }
    }

    /**
     * Equality grammar<p/>
     * The rule for equality is as follow:<p/>
     * <code>equality -> comparison (( "!=" | "==" ) comparison)* ;</code>
     * <p>Note: if the parser never encounters an equality operator, then it never
     * enters the <code>while</code> loop, In that case, this method effectively calls
     *  and returns <code>comparison()</code> method. In this way, this method matches
     * an equality operator <i>or anything of higher procedure</i>.</p>
     * @return
     */
    private Expr equality()
    {
        //take result of the first comparison nonterminal and store it in a local variable.
        Expr expr = comparison();

        //Map the ( ... )* loop in the rule to a while loop.
        //Iterate until encounter either != or == token
        //As we zip through a sequence of equality expression, that
        //creates a left-associative nested tree of binary operator nodes.
        while(match(BANG_EQUAL, EQUAL_EQUAL))
        {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        //if execution flow bypassed while() loop meaning there is no right-hand operand, then
        //we must be done with the sequence of equality operators
        return expr;
    }

    /**
     * The rule for comparison is as follow:<p/>
     * <code>comparison -> term (( ">" | ">=" | "<" | "<=" ) term)* ;</code><p/>
     * The grammar rule is virtually identical to
     * equality and so is the corresponding code.
     * @return
     */
    private Expr comparison()
    {
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * The rule for term is as follow:<p/>
     * <code>term -> factor (( "-" | "+" ) factor)*;</code><p/>
     * @return
     */
    private Expr term()
    {
        Expr expr = factor();
        
        while(match(MINUS, PLUS))
        {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * The rule for factor is as follow:<p/>
     * <code>factor -> unary (( "/" | "*" ) unary)*;</code><p/>
     * @return
     */
    private Expr factor()
    {
        Expr expr = unary();
    
        while (match(SLASH, STAR))
        {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    /**
     * The rule for unary is as follow:<p/>
     * <code>unary -> ( "!" | "-" ) unary | primary;</code><p/>
     * 
     * @return
     */
    private Expr unary()
    {
        if (match(BANG, MINUS))
        {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
    
        return primary();
    }

    /**
     * The rule for primary is as follow:<p/>
     * <code>primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")";</code><p/>
     * @return
     */
    private Expr primary()
    {
        if (match(FALSE))
            return new Expr.Literal(false);

        if (match(TRUE))
            return new Expr.Literal(true);

        if (match(NIL))
            return new Expr.Literal(null);
    
        if (match(NUMBER, STRING))
            return new Expr.Literal(previous().literal);

        if (match(IDENTIFIER))
            return new Expr.Variable(previous());
    
        if (match(LEFT_PAREN))
        {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    /**
     * Helper method called from <code>primary()</code> method
     * to examine that the current token matches the closing parenthesis.
     * If that's true, the current token will be consumed.
     * @param type
     * @param message
     * @return Token
     */
    private Token consume(TokenType type, String message)
    {
        if (check(type))
            return advance();
    
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message)
    {
        Main.error(token, message);
        return new ParseError();
    }

    /**
     * Synchronization - a process when a parser gets back to parsing
     * from the last leaved position in the sequence of forthcoming tokens
     * aligned such that the next token does match the rule being parsed.<p/>
     * This method discards tokens until we're right at the beginning of the
     * next statement.
     */
    private void synchronize()
    {
        advance();

        while (!isAtEnd())
        {
            if (previous().type == SEMICOLON)
                return;
            
            //if current token is one of the case, then we're at the beginning of the new statement.
            //That means that parser went right through a statement with syntax error.
            switch (peek().type)
            {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    /**
     * Checks to see if the current token has any of the given types.
     * If so, it consumes the token and returns true.<p/>
     * @param types
     * @return
     */
    private boolean match(TokenType... types)
    {
        for (TokenType type : types)
        {
            if(check(type))
            {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the current token is of the given type.<p/>
     * Unlike <code>match()</code> method it never consumes the
     * token, just only looks at it.
     * @param type
     * @return
     */
    private boolean check(TokenType type)
    {
        if (isAtEnd())
            return false;
        
        return peek().type == type;
    }

    /**
     * This method consumes the current token and returns it,
     * similar to how our scanner's corresponding method
     * crawled through characters.
     * @return
     */
    private Token advance()
    {
        if (!isAtEnd())
            current++;
        
        return previous();
    }

    /**
     * Returns true if Parser encountered with EOF token.
     * @return
     */
    private boolean isAtEnd()
    {
        return peek().type == EOF;
    }

    /**
     * Returns the current token under examination.
     * @return
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the most recently consumed token.
     * @return
     */
    private Token previous() {
        return tokens.get(current - 1);
    }
}
