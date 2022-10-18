package ru.beeline.interpreter.lexer;

import ru.beeline.interpreter.exception.InterpreterException;
import ru.beeline.interpreter.token.Token;
import ru.beeline.interpreter.token.TokenConstants;

public class Lexer implements TokenConstants
{
    //private Lexer lexer = null;
    private Token token = null;
    private String input;
    private int inputLen = 0;
    public int currPos = 0;         // current char
    public int nextPos = 0;         // the char right after current one
    public char literal = (char)0;  // char under examination
    
    public Lexer(String input)
    {
        this.input = input;
        inputLen = this.input.length();
    }

    public Token nextToken()
    {
        readCharacter();
        skipWhiteSpace();

        switch(literal)
        {
            case '=': {
                char tmp = peekNextCharacter();
                if (tmp == '=') {
                    tmp = literal;
                    readCharacter();
                    token = newToken(EQ, tmp, literal);
                }else
                    token = newToken(ASSIGN, literal);
            }break;
            case '+': token = newToken(PLUS, literal);      break;
            case '-': token = newToken(MINUS, literal);     break;
            case '!': {
                char tmp = peekNextCharacter();
                if (tmp == '=') {
                    tmp = literal;
                    readCharacter();
                    token = newToken(NOT_EQ, tmp, literal);
                }else
                
                token = newToken(BANG, literal);
            }break;
            case '*': token = newToken(ASTERISK, literal);  break;
            case '/': token = newToken(SLASH, literal);     break;

            case '<': token = newToken(LT, literal);        break;
            case '>': token = newToken(GT, literal);        break;
            case ';': token = newToken(SEMICOLON, literal); break;
            case ',': token = newToken(COMMA, literal);     break;

            case '(': token = newToken(LPAREN, literal);    break;
            case ')': token = newToken(RPAREN, literal);    break;
            case '{': token = newToken(LBRACE, literal);    break;
            case '}': token = newToken(RBRACE, literal);    break;

            case (char)0:  token = newToken(EOF, (char)0);  break;
            default:
            {
                token = new Token(null, null);
                if (isLetter(literal))
                {
                    token.literal = readLiteral();
                    token.type = token.lookUpType();
                }else if(isDigit(literal))
                {
                    token.literal = readNumber();
                    token.type = INT;
                }else
                {
                    token.literal =  String.valueOf(literal);
                    token.type = ILLEGAL;
                }
            }
        }
        return token;
    }

    private void readCharacter()
    {
        if (nextPos >= inputLen)
            literal = (char)0;
        else
            literal = input.charAt(nextPos);
        
        currPos = nextPos++;
    }

    private String readLiteral()
    {
        int beginIdx = currPos;
        char nextChar = peekNextCharacter();

        while(isLetter(nextChar))
        {
            readCharacter();
            nextChar = peekNextCharacter();
        }

        return input.substring(beginIdx, nextPos);
    }

    private String readNumber()
    {
        int beginIdx = currPos;
        char nextChar = peekNextCharacter();

        while(isDigit(nextChar))
        {
            readCharacter();
            nextChar = peekNextCharacter();
        }

        return input.substring(beginIdx, nextPos);
    }

    private char peekNextCharacter()
    {
        if (nextPos >= inputLen)
            return literal = (char)0;
        else
            return input.charAt(nextPos);
    }

    private boolean isLetter(char ch)
    {
        return (('a' <= ch) && (ch <= 'z')) || (('A' <= ch) && (ch <= 'Z')) || (ch == '_');
    }

    private boolean isDigit(char ch)
    {
        return (('0' <= ch) && (ch <= '9'));
    }

    private void skipWhiteSpace()
    {
        while(literal == ' ' || literal == '\t' || literal == '\r' || literal == '\n')
        {
            readCharacter();
        }
    }

    private Token newToken(String type, char literal)
    {
        return new Token(type, String.valueOf(literal));
    }

    private Token newToken(String type, char lit1, char lit2)
    {
        return new Token(type, String.valueOf(lit1) + String.valueOf(lit2));
    }
}
