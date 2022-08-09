package ru.beeline.interpreter.lexer;

import ru.beeline.interpreter.exception.InterpreterException;
import ru.beeline.interpreter.token.Token;
import ru.beeline.interpreter.token.TokenConstants;

public class Lexer
{
    //private Lexer lexer = null;
    public String input;
    public int position;
    public int readPosition;
    public char ch;

    public Lexer(String input)
    {
        this.input = input;
        this.position = 0;
        this.readPosition = 0;
        this.ch = 0;
        readChar();
    }

    public Lexer getLexer(String input)
    {
        return new Lexer(input);
    }

    private void readChar()
    {
        if (readPosition >= input.length())
            ch = (byte)0;
        else
            ch = input.charAt(readPosition);
        
        position = readPosition++;
    }

    public Token nextToken()
    {
        Token tok = null;
        switch(ch)
        {
            case '=': tok = newToken(TokenConstants.ASSIGN, ch); break;
            case ';': tok = newToken(TokenConstants.SEMICOLON, ch); break;
            case '(': tok = newToken(TokenConstants.LPAREN, ch); break;
            case ')': tok = newToken(TokenConstants.RPAREN, ch); break;
            case ',': tok = newToken(TokenConstants.COMMA, ch); break;
            case '+': tok = newToken(TokenConstants.PLUS, ch); break;
            case '{': tok = newToken(TokenConstants.LBRACE, ch); break;
            case '}': tok = newToken(TokenConstants.RBRACE, ch); break;
            case 0: tok = newToken(TokenConstants.EOF, (char)0x20); break;
            default:
                throw new InterpreterException("Unexpected token " + String.valueOf(ch));
        }
        readChar();
        return tok;
    }

    public Token newToken(String type, char literal)
    {
        return new Token(type, String.valueOf(literal));
    }
}
