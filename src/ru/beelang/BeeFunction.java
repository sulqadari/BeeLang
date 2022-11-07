package ru.beelang;

import java.util.List;

import ru.beelang.nativeFuncs.BeeCallable;

public class BeeFunction implements BeeCallable
{
    private final Stmt.Function declaration;
    private final Environment closure;

    /**
     * 
     * @param declaration
     * @param closure to capture the current environment. Represents the lexical
     * scope surrounding the function declaration.
     */
    BeeFunction(Stmt.Function declaration, Environment closure)
    {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public String toString()
    {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity()
    {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token token)
    {
        // gets its own environment
        Environment environment = new Environment(closure);
        int size = declaration.params.size();
        
        for(int i = 0; i < size; ++i)
            environment.define(declaration.params.get(i).lexeme, arguments.get(i), token);
        
        try {
            interpreter.executeBlock(declaration.body, environment);
        }catch(Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
}
