package ru.beelang.nativeFuncs;

import java.util.List;

import ru.beelang.Interpreter;
import ru.beelang.Token;

public class Clock implements BeeCallable
{
    @Override
        public int arity()
        {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token token)
        {
            return (int)System.currentTimeMillis() / 1000;
        }

        @Override
        public String toString()
        {
            return "<native clock";
        }
}