package ru.beeline.interpreter.exception;

public class InterpreterException extends RuntimeException
{
    String message;
    public InterpreterException(String message)
    {
        super();
        this.message = message;
    }
}
