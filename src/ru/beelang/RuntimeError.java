package ru.beelang;

/**
 * This class tracks the <code>Token<code> that identifies where in the user's code
 * the runtime error came from.<p/>
 * As with static errors, this helps the user know where to fix their code.
 */
public class RuntimeError extends RuntimeException
{
    final Token token;

    public RuntimeError(Token token, String message)
    {
        super(message);
        this.token = token;
    }
}
