package ru.beeline.interpreter.token;

public class Token implements TokenConstants
{
    public String type;
    public String literal;

    public Token(String type, String literal)
    {
        this.type = type;
        this.literal = literal;
    }
}