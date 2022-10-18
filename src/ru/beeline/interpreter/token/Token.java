package ru.beeline.interpreter.token;

import java.util.Hashtable;

public class Token implements TokenConstants
{
    public String type;
    public String literal;

    Hashtable<String, String> keyWords = new Hashtable<String, String>();
    
    public Token(String type, String literal)
    {
        this.type = type;
        this.literal = literal;
        keyWords.put("fn", FUNCTION);
        keyWords.put("let", LET);
        keyWords.put("true", TRUE);
        keyWords.put("false", FALSE);
        keyWords.put("if", IF);
        keyWords.put("else", ELSE);
        keyWords.put("return", RETURN);
    }

    public String lookUpType()
    {
        if(keyWords.containsKey(literal))
            return keyWords.get(literal);
        else
            return IDENT;
    }

}