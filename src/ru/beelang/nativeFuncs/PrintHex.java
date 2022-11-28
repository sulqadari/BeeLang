package ru.beelang.nativeFuncs;

import java.util.Arrays;
import java.util.List;

import ru.beelang.Interpreter;
import ru.beelang.Token;

public class PrintHex  implements BeeCallable
{

    // negative value designates variable length of params.
    final int arity = -1;

    @Override
    public int arity()
    {
        return arity;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token token)
    {
        if (arguments.size() < 1)
        {
            System.out.println();
            return null;
        }

        if ((arguments.get(0)) instanceof byte[])
        {
            byte[] array;// = (byte[])(arguments.get(0));

            for (int i = 0; i < arguments.size(); ++i)
            {
                array  = (byte[])(arguments.get(i));

                for (byte b : array)
                {
                    System.out.printf("%02X", b);
                }
            }
        }
        else
        {
            Object[] temp = arguments.toArray();
            Arrays.stream(temp).forEach(e -> System.out.printf("%02X", e));
        }
        
        System.out.println();
        return null;
    }
    
    @Override
    public String toString()
    {
        return "<native print";
    }
}
