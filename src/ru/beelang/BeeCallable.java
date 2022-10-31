package ru.beelang;

import java.util.List;

interface BeeCallable
{
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
