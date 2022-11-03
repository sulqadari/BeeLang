package ru.beelang.nativeFuncs;

import java.util.List;

import ru.beelang.Interpreter;
import ru.beelang.Token;

public class Print implements BeeCallable
{
    // negative value stands for variable length of params.
    final int arity = -1;

    @Override
    public int arity()
    {
        return arity;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token token)
    {
        for(Object arg : arguments)
        {
            System.out.print(stringify(arg));
        }

        return null;
    }

    @Override
    public String toString()
    {
        return "<native print";
    }

    private String stringify(Object object)
    {
        if (null == object)
            return "nil";
        
        if (object instanceof Double)
        {
            String text = object.toString();
            if(text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
    
}
