package ru.beelang;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is intended to store bindings that associate variables to their values.<p/>
 */
public class Environment
{
    final Environment enclosing;
    /** HashMap of the variable-value bindings. Uses Strings as a key, not tokens. */
    private final Map<String, Object> values = new HashMap<>();

    /**
     * for the global scope's environment, which ends the chain.
     */
    Environment()
    {
        enclosing = null;
    }

    /**
     * Creates a new local scope nested inside the given outer one.
     * @param enclosing
     */
    Environment(Environment enclosing)
    {
        this.enclosing = enclosing;
    }

    Object get(Token name)
    {
        if (values.containsKey(name.lexeme))
            return values.get(name.lexeme);
        
        // handle the 'parent-pointer tree',
        // i.e. lookup for given token in the nearest outer scope
        if (null != enclosing)
            return enclosing.get(name);
        
        throw new RuntimeError(name, "Undefined variable '" +name.lexeme + "'.");
    }

    /**
     * The key difference between this method and <code>define()</code> is that
     * this method isn't allowed to create a new variable.<p/>
     * In terms of out implementation, that means it's a runtime error if the key
     * doesn't already exist in the environment's variable map.
     * @param name
     * @param value
     */
    void assign(Token name, Object value)
    {
        if (values.containsKey(name.lexeme))
        {
            values.put(name.lexeme, value);
            return;
        }

        // if the variable isn't in this environment, it checks the outer one, recursively.
        if (null != enclosing)
        {
            enclosing.assign(name, value);
            return;
        }
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Variable definition.<p/>
     * Binds a new name to a value.
     * @param name
     * @param value
     * @throws RuntimeError if variable have already been defined
     */
    void define(String name, Object value, Token token)
    {
        if (values.containsKey(name))
            throw new RuntimeError(token, "identifier '" + name + "' is already in use.");

        values.put(name, value);
    }


}
