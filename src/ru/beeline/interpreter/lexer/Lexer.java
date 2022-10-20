package ru.beeline.interpreter.lexer;

import ru.beeline.interpreter.token.Token;

/**
 * This class receives input string and splits it into separate tokens.
 */
public class Lexer
{
    private String input = "";
    private int inputLen = 0;
    public int currPos = 0;         // current char
    public int nextPos = 0;         // the char right after current one
    public char literal = (char)0;  // char under examination
    
    public Lexer(String input)
    {
        this.input = input;
        inputLen = this.input.length();
        Token.initKeywords();
    }

    public Token nextToken()
    {
        Token token = null;
        readCharacter();
        skipWhiteSpace();

        switch(literal)
        {
            case '+': token = newToken(Token.PLUS, literal);      break;
            case '-': token = newToken(Token.MINUS, literal);     break;
            case '*': token = newToken(Token.ASTERISK, literal);  break;
            case '/': token = newToken(Token.SLASH, literal);     break;
            case '<': token = newToken(Token.LT, literal);        break;
            case '>': token = newToken(Token.GT, literal);        break;
            case ';': token = newToken(Token.SEMICOLON, literal); break;
            case ',': token = newToken(Token.COMMA, literal);     break;
            case '(': token = newToken(Token.LPAREN, literal);    break;
            case ')': token = newToken(Token.RPAREN, literal);    break;
            case '{': token = newToken(Token.LBRACE, literal);    break;
            case '}': token = newToken(Token.RBRACE, literal);    break;
            case 0:   token = newToken(Token.EOF, (char)0);       break;
            case '=': {
                char tmp = peekNextCharacter();
                if (tmp == '=') {
                    tmp = literal;
                    readCharacter();
                    token = newToken(Token.EQ, tmp, literal);
                }else
                    token = newToken(Token.ASSIGN, literal);
            }break;
            case '!': {
                char tmp = peekNextCharacter();
                if (tmp == '=') {
                    tmp = literal;
                    readCharacter();
                    token = newToken(Token.NOT_EQ, tmp, literal);
                }else
                
                token = newToken(Token.BANG, literal);
            }break;
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
                    token.type = Token.INT;
                }else
                {
                    token.literal =  String.valueOf(literal);
                    token.type = Token.ILLEGAL;
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
            return literal = 0;
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
