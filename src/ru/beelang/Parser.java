package ru.beelang;

import java.util.ArrayList;
import java.util.Arrays;
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
 * 
 * Syntax Grammar:<p/>
 * <p>program - > declaration* EOF;</p>
 * 
 * Declarations:<p/>
 * A program is a series of declarations, which are the statements that
 * bind new identifiers or any of the other statement types.
 * <ul>
 * <li>declaration    -> <code>classDecl | funDecl | varDecl | statement;</code></li>
 * <li>classDecl      -> <code>"class" IDENTIFIER ("<" IDENTIFIER)? "{" function* "}";</code></li>
 * <li>funDecl        -> <code>"fun" function;</code></li>
 * <li>varDecl        -> <code>"var" IDENTIFIER ("=" expression)? ";";</code></li>
 * <li>arrDecl        -> <code>"arr" IDENTIFIER ("[" "]" "{" NUMBER+ "}") | ("[" NUMBER "]" "{" NUMBER* "}") </code></li>";"
 * </ul>
 * 
 * Statements:<p/>
 * The remaining statement rules produce side effects, but do not introduce bindings.
 * <ul>
 * <li>statement      -> <code>exprStmt | forStmt | ifStmt | printStmt | returnStmt | whileStmt | block ;</code></li>
 * <li>exprStmt       -> <code>expression ";" ;</code></li>
 * <li>forStmt        -> <code>"for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;</code></li>
 * <li>ifStmt         -> <code>"if" "(" expression ")" statement ( "else" statement )? ;</code></li>
 * <li>printStmt      -> <code>"print" expression ";" ;</code></li>
 * <li>returnStmt     -> <code>"return" expression? ";" ;</code></li>
 * <li>whileStmt      -> <code>"while" "(" expression ")" statement ;</code></li>
 * <li>block          -> <code>"{" declaration* "}" ;</code></li>
 * </ul>
 * <p><b>Note</b> that <code>block</code> is a statement rule, but is also used as a nonterminal in
 * a couple of other rules for things like function bodies.</p>
 * 
 * <p>Expressions:</p>
 * Expressions produce values. We use a separate rule for each precedence level to make it explicit.
 * <ul>
 * <li>expression     -> <code>assignment ;</code></li>
 * <li>assignment     -> <code>( call "." )? IDENTIFIER "=" assignment logic_or ;</code></li>
 * <li>logic_or       -> <code>logic_and ( "or" logic_and )* ;</code></li>
 * <li>logic_and      -> <code>equality ( "and" equality )* ;</code></li>
 * <li>equality       -> <code>comparison ( ( "!=" | "==" ) comparison )* ;</code></li>
 * <li>comparison     -> <code>term ( ( ">" | ">=" | "<" | "<=" ) term )* ;</code></li>
 * <li>term           -> <code>factor ( ( "-" | "+" ) factor )* ;</code></li>
 * <li>factor         -> <code>unary ( ( "/" | "*" ) unary )* ;</code></li>
 * <li>unary          -> <code>( "!" | "-" ) unary | call ;</code></li>
 * <li>call           -> <code>primary ( "(" arguments? ")" | "." IDENTIFIER )* ;</code></li>
 * <li>increment      -> <code>primary (IDENTIFIER ("++" | "--")) ;</code></li>
 * <li>primary        -> <code>"true" | "false" | "nil" | "this" | NUMBER | STRING | IDENTIFIER | "(" expression ")" | "super" "." IDENTIFIER ;</code></li>
 * 
 * </ul>
 * 
 * Utility rules:<p/>
 * Reusable helper rules.
 * <ul>
 * <li>function       -> <code>IDENTIFIER "(" parameters? ")" block ;</code></li>
 * <li>parameters     -> <code>IDENTIFIER ( "," IDENTIFIER )* ;</code></li>
 * <li>arguments      -> <code>expression ( "," expression )* ;</code></li>
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

    private Stmt declaration()
    {
        try
        {
            if (match(FUN))
                return function("function");
            if (match(VAR))
                return varDeclaration();
            
            return statement();
        }catch(ParseError error)
        {
            synchronize();
            return null;
        }
    }

    private Stmt.Function function(String kind)
    {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name");
        List<Token> parameters = new ArrayList<>();

        // handles the zero parameter case
        if (!check(RIGHT_PAREN))
        {
            // parses parameters as long as we find commas to separate them.
            do {
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            }while(match(COMMA));

            if(parameters.size() >= 255)
                error(peek(), "Can't have more that 255 parameters.");
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varDeclaration()
    {
        // retrieve current token and move to subsequent one
        Token name = consume(IDENTIFIER, "Expect variable name.");
        
        Expr initializer = null;

        if (match(EQUAL))
        {
            //if (match(QUOTE)){ /* just consume openning quote in case we initialize an array */}
            initializer = expression();
        }
        
        // declaration without initialization.
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    // private Stmt arrDeclaration()
    // {
    //     // retrieve current token and move to subsequent one
    //     Token name = consume(IDENTIFIER, "Expect array name.");
    //     consume(LEFT_SQR_BRACKET, "Expect left square bracket.");
    //     Expr size = expression();
    //     consume(RIGHT_SQR_BRACKET, "Expect right square bracket.");
    // }

    /**
     * Parses a statements.
     * @return
     */
    private Stmt statement()
    {
        if (match(FOR))
            return forStatement();
        
        if (match(IF))
            return ifStatement();
        
        if (match(RETURN))
            return returnStatement();
        
        if (match(WHILE))
            return whileStatement();
        
        if (match(LEFT_BRACE))
            return new Stmt.Block(block());
        
        return expressionStatement();
    }

    private Stmt expressionStatement()
    {
        Expr expr = expression();

        consume(SEMICOLON, "Expect ';' after expression");

        return new Stmt.Expression(expr);
    }

    /**
     * Expression grammar<p/>
     * The rule for expression is as follow:<p/>
     * <code>expression -> equality;</code>
     * @return
     */
    private Expr expression()
    {
        return assignment();
    }

    /**
     * Handles assignment expressions (l-value statements).<p/>
     * The trick is that right before we create the assignment expression node,
     * we look at the left-hand side expression and figure out what kind of assignment
     * target it is. We convert the r-value expression node into an l-value representation.<p/>
     * This means we can parse the left-hand side as <i>if it were</i> an expression and then
     * after the fact produce a syntax tree that turns it into an assignment target.
     * 
     * @return
     */
    private Expr assignment()
    {
        Expr expr = or();
        if(match(EQUAL))
        {               
            Token equals = previous();
            // Since assignment is right-associative, we recursively call
            // assignment() to parse the right-hand side.
            Expr value = assignment();

            // figure out what kind of assignment target it is and
            // convert the r-value expression node into an l-value representation.
            if (expr instanceof Expr.Variable)
            {
                // This conversion works because it turns out that every valid
                // assignment target happens to also be valid syntax as a normal expression.
                // Consider a complex field assignment like:
                // newPoint(x + 2, 0).y = 3;
                // newPoint(x + 2, 0).y;
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            //report an error if left-hand side isn't a valid assignment target.
            error(equals, "Invalid assignment target");
        }
        return expr;
    }

    private Expr or()
    {
        Expr expr = and();
        while(match(OR))
        {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and()
    {
        Expr expr = equality();
        while(match(AND))
        {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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
        //Expr expr = term();
        Expr expr = arrIdx();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr arrIdx()
    {
        Expr expr = term();

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
     * <code>unary -> ( "!" | "-" | "--" | "++" ) unary | primary;</code><p/>
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

        return call();
    }

    private Expr call()
    {
        Expr expr = increment();
        while(true)
        {
            if (match(LEFT_PAREN))
                expr = finishCall(expr);
            else
                break;
        }

        return expr;
    }

    private Expr increment()
    {
        Expr expr = array();

        if (match(INCREMENT, DECREMENT))
        {
            Token sign = previous();

            if (!(expr instanceof Expr.Variable))
                error(sign, "Invalid increment target");
            // extract Token obj from Expr.Variable obj
            Token name = ((Expr.Variable)expr).name;
            return new Expr.Increment(name, sign);
        }

        return expr;
    }

    private Expr array()
    {
        if (match(QUOTE))
        {
            advance();
            return new Expr.Literal(previous().literal);
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

    // ============================================================ //
    // ======================== Statements ======================== //
    // ============================================================ //
    /**
     * Desugared implementation which means
     * there is no dedicated syntax tree provided, instead
     * 'for loop' is constructed based on 'while' loop.
     * @return
     */
    private Stmt forStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        
        // initializer
        Stmt initializer;
        if (match(SEMICOLON))
            initializer = null;
        else if (match(VAR))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();
        

        // condition
        Expr condition = null;
        if (!check(SEMICOLON))
            condition = expression();
        
        consume(SEMICOLON, "Expect ';' after loop condition.");

        // increment
        Expr increment = null;
        if (!check(RIGHT_PAREN))
            increment = expression();
        
        consume(RIGHT_PAREN, "Expect ')' after for clause.");

        // body
        Stmt body = statement();

        // The increment executes after the body in each iteration of the loop.
        if (null != increment)
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

        // take the condition and the body and build the loop using a primitive while loop
        if (null == condition)
            condition = new Expr.Literal(true); // true for infinite loop

        body = new Stmt.While(condition, body);
        
        // if there is an initializer, it runs once before the entire loop
        if (null != initializer)
            body = new Stmt.Block(Arrays.asList(initializer, body));
        
        return body;
    }

    private Stmt whileStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement()
    {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        
        if (match(ELSE))
        {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt returnStatement()
    {
        Token keyword = previous();
        Expr value = null;

        if(!check(SEMICOLON))
            value = expression();
        
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
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

    // ========================================================= //
    // ======================== Helpers ======================== //
    // ========================================================= //
    private Expr finishCall(Expr callee)
    {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN))
        {
            do {
                arguments.add(expression());
            }while(match(COMMA));

            if (arguments.size() >= 255)
                    error(peek(), "Can't have more than 255 arguments.");
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments list.");
        return new Expr.Call(callee, paren, arguments);
    }

    /**
     * Helper method which examines that the current token matches
     * the given <code>type</code>.<p/>
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

    private Token peekNext()
    {
        return tokens.get(current + 1);
    }

    /**
     * Returns the most recently consumed token.
     * @return
     */
    private Token previous() {
        return tokens.get(current - 1);
    }
}
