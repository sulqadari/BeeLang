package ru.beelang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.beelang.utils.Hlp;

import static ru.beelang.TokenType.*;

/**
 * <p>Scans the raw input string of code.</p>
 * Actualy this class not only scans and validates the input,
 * but also performs functionality of a <code>Lexer</code> to product the tokens.
 * <p>These tokens will be used by <code>Parser</code> to generate appropriate code
 * representation. After that the <code>Interpreter</code> will consume this representation.</p>
 */
public class Scanner
{
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        //keywords.put("arr",    QUOTE);
        keywords.put("var",    VAR);
        keywords.put("return", RETURN);
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
            case '(': addToken(LEFT_PAREN);     break;
            case ')': addToken(RIGHT_PAREN);    break;
            case '{': addToken(LEFT_BRACE);     break;
            case '}': addToken(RIGHT_BRACE);    break;
            case '[': addToken(LEFT_BRACKET);   break;
            case ']': addToken(RIGHT_BRACKET);  break;
            case ',': addToken(COMMA);          break;
            case '.': addToken(DOT);            break;
            case ';': addToken(SEMICOLON);      break;
            case '*': addToken(STAR);           break;
            case '-': addToken(match('-') ? DECREMENT : MINUS);       break;
            case '+': addToken(match('+') ? INCREMENT : PLUS);        break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG);       break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL);     break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS);       break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '\'': byteArray();             break;
            case '/': comment();                break;
            case '"': string();                 break;
            case '\n': line++;                  break;  // lines counter
            case ' ': case '\r': case '\t':     break;  // Ignore whitespaces.
            default:  userDefined(c);           break;
        }
    }

    private void comment()
    {
        // if successive character is slash too...
        if (match('/'))
        {   // ..go until the end of the line.
            while (peek() != '\n' && !isAtEnd())
                advance();
        }else   // otherwise we a about to add devision sign.
        {
            addToken(SLASH);
        }
    }

    private void userDefined(char c)
    {
        if (isDecimal(c))
        {
            if (inHex(c))
                hexadecimal();
            else
                decimal();

        }else if (isAlpha(c))
        {
            identifier();
        }else {
            Main.error(line, "Unexpected character.");
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

    private void decimal()
    {
        while (isDecimal(peek()))
            advance();

        try {
            addToken(NUMBER, Integer.parseInt(source.substring(start, current), 10));
        }catch(NumberFormatException e)
        {
            Main.error(line, "Interpreter accepts positive values only." + '\n' +
                              "Note: 2147483648 and above implicitly converts to negative int." + '\n');
        }
    }

    private void hexadecimal()
    {
        while (isHex(peek()))
            advance();
        
        // trim preceding 0x
        String value = source.substring(start + 2, current);
        try {
            addToken(NUMBER, Integer.parseInt(value, 16));
        }catch(NumberFormatException e)
        {
            Main.error(line, "Interpreter accepts positive values only."
                            + '\n' + "Note: 0x80000000 and above implicitly converts to int." + '\n');
        }
    }

    /**
     * Returns <code>true</code> if current character is within following ranges:
     * [a-f] [A-F] [0-9]
     * @param c
     * @return
     */
    private boolean isHex(char c)
    {
        return (c >= 'A' && c <= 'F') ||
               (c >= 'a' && c <= 'f') ||
               (c >= '0' && c <= '9');
    }

    private void byteArray()
    {
        addToken(QUOTE);
        
        while ((peek() != '\'') && !isAtEnd())
        {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd())
        {
            Main.error(line, "Unterminated byte string.");
            return;
        }

        // consume the closing '.
        advance();
        
        // Trim the surrounding square brackets.
        addToken(ARR, Hlp.toByteArray(source.substring(start + 1, current - 1)));
    }

    /**
     * <p>Handles string.</p>
     * When the current token is of type '"' this method
     *  advances <code>current</code> until it encounters with the closing double quote.
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

        // Consume the closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Examines current character for equality with the given value.
     * If they match then <code>corrent</code> is shifted forward.
     * @param expected
     * @return
     */
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

    private char peekPrevious()
    {
        return source.charAt(current - 1);
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }
        
    /**
     * Checks if the current token is letter or digit, but not
     * special character
     * @param c character under examination
     * @return true if it is. False otherwise
     */
    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDecimal(c);
    }

    /**
     * Returns <code>true</code> if current character is within following ranges:
     * [a-z]  [A-Z] _
     * @param c
     * @return
     */
    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               (c == '_');
    }

    /**
     * Returns <code>true</code> if current character is within ranges [0-9]
     * @param c
     * @return
     */
    private boolean isDecimal(char c)
    {
        return c >= '0' && c <= '9';
    }

    private boolean inHex(char c)
    {
        if (peek() == 'x' && peekPrevious() == '0')
        {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Returns current character to be processed.<p>
     * Before method returns the <code>current</code> is incremented
     * so the next time this method being called the <code>current</code> will
     * point to successive character in input stream.
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
