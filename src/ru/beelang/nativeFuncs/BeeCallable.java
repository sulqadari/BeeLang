package ru.beelang.nativeFuncs;

import java.util.List;

import ru.beelang.Interpreter;
import ru.beelang.Token;

public interface BeeCallable
{
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments, Token token);
    //Object call(Interpreter interpreter, byte[] arguments, Token token);
}
