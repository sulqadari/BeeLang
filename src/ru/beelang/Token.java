package ru.beelang;

/**
 * Token - the basic self-sufficient element of BeeLang
 */
public class Token
{
    /**
     * Represents a keywords (reserved words) of the language's grammar.
     * This field adds to the lexeme additional information about how it
     * should be interpreted. In other words, this field categorizes token.
     */
    final TokenType type;

    /** <p>lexeme:</p>
     * a basic lexical unit of a language consisting of one word or several words,
     * the elements of which do not separately convey the meaning of the whole.
     * <p>
     * In BeeLang each element of the following statement is lexeme:
     * </p>
     * <p><code>var language = "BeeLang";</code></p>
     * 
     * */
    public final String lexeme;

    /**
     * The value retrieved from the lexeme which is used by the interpreter.
     * <p>As an example consider convertion the <code>lexeme</code> field containg
     * double value <code>42.5</code> to <code>literal = Double.parseDouble(lexeme)</code></p>
     */
    final Object literal;

    /**
     * For error handling purposes. Keeps track of the line at which
     * this token have appeared.
     */
    final int line; 

    Token(TokenType type, String lexeme, Object literal, int line)
    {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString()
    {
        return String.format("%s %s %s", type, lexeme, literal);
    }
}
