package ru.beelang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.beelang.TokenType.*;

/**
 * <p>Scans the raw input string of code.</p>
 * Actualy this class not only scans the input for correct value
 * but also performs functionality of a <code>Lexer</code> to product the tokens.
 * <p>These tokens will be used by <code>Parser</code> to generate appropriate code
 * representation. After that the <code>Interpreter</code> will consume this representation.</p>
 */
public class Scanner
{
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("var",    VAR);
        keywords.put("return", RETURN);
        keywords.put("print",  PRINT);
        keywords.put("fun",    FUN);
        keywords.put("class",  CLASS);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("nil",    NIL);

        keywords.put("or",     OR);
        keywords.put("and",    AND);
        
        keywords.put("if",     IF);
        keywords.put("else",   ELSE);
        keywords.put("true",   TRUE);
        keywords.put("false",  FALSE);

        keywords.put("for",    FOR);
        keywords.put("while",  WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;      // points to the first character in the lexeme being scanned
    private int current = 0;    // points at the character currently being considered
    private int line = 1;       // tracks what source line 'current' is on, so we can produce tokens that know their location.

    Scanner(String source)
    {
        this.source = source;
    }

    List<Token> scanTokens()
    {
        while (!isAtEnd())
        {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * The 'heart' of the scanner.
     * <p>The scanner figures out what lexeme the character belongs to, and consumes it and any following
     * characters that are part of that lexeme. When it reaches the end of that lexeme, it emits a token.</p>
     */
    private void scanToken()
    {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            // case '&': addToken(AND); break;
            // case '|': addToken(OR); break;
            case '/':
                if (match('/'))
                {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                }else
                {
                    addToken(SLASH);
                }
            break;
            // Ignore whitespaces.
            case ' ': case '\r': case '\t':
            break;
            case '\n':
                line++;
            break;
            case '"': string(); break;
            default:
            {
                if (isDigit(c))
                {
                    number();
                } else if (isAlpha(c))
                {
                    identifier();
                } else
                {
                    Main.error(line, "Unexpected character.");
                }
            }break;
        }
    }

    /**
     * <p>Assgins current token which represents an idnetifier (reserved or user defined)
     * with associated keyword.</p>
     * If scanned token is not one from the list defined in <code>Map<String, TokenType> keywords</code>
     * e.g. it isn't 'keyword', then it is tacitly assumed as user defined identifier (myVar, etc.)
     */
    private void identifier()
    {
        while (isAlphaNumeric(peek()))
            advance();
        
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        
        if (type == null)
            type = IDENTIFIER;
        
        addToken(type);
    }
    
    private void number()
    {
        while (isDigit(peek()))
            advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext()))
        {
            // Consume the "."
            advance();

            while (isDigit(peek()))
                advance();
        }

        //TODO: replace Double with Integer
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               (c == '_');
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    /**<code></code>
     * <p>Handles string.</p>
     * When the current token is of type '"' this method is invoked from 
     * <code>scanToken()</code> and advances <code>current</code> until
     * it encounters with the closing double quote.
     * <p>Then the substring <code>start + 1, current - 1</code> is stored using
     * overloaded <code>addToken()</code> method.</p>
     */
    private void string()
    {
        while ((peek() != '"') && !isAtEnd())
        {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd())
        {
            Main.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected)
    {
        if (isAtEnd())
            return false;
        
        if (source.charAt(current) != expected)
            return false;
    
        current++;
        return true;
    }

    /**
     * Peeks current character without shifting <code>current</code>
     * @return current character
     */
    private char peek()
    {
        if (isAtEnd())
            return '\0';
        
        return source.charAt(current);
    }

    /**
     * Peeks the character following the <code>current</code> one without
     * shifting <code>current</code> forward
     * @return
     */
    private char peekNext()
    {
        if (current + 1 >= source.length())
            return '\0';
        
        return source.charAt(current + 1);
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }

    /**
     * Returns current character to be processed.<p>
     * Before method returns the <code>current</code> is incremented
     * thus next time this method being called the latter variable will
     * point to the next character in input stream.
     * @return
     */
    private char advance()
    {
        return source.charAt(current++);
    }

    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
