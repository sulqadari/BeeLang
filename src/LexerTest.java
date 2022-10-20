import java.util.Hashtable;

import ru.beeline.interpreter.lexer.Lexer;
import ru.beeline.interpreter.token.Token;

public class LexerTest
{
    public static void main(String[] args)
    {
        TestNextToken();
    }
    
    
    public static void TestNextToken()
    {
        String input = "";
        int inLen = 0;

        Lexer lexer;
        Token token;
        Hashtable<String, String> refVals = new Hashtable<String, String>();

        initHashTable(refVals);
        input = 
        (
            "if(10 == ten) {"
            + "    return true;"
            +"};"
            + "if(10 != ten) {"
            + "    return false;"
            +"};"
            + "let five = 5;"
            + "let ten = 10;"
            + "let add = fn(x, y) {"
            + "   x + y;"
            + "};"
            + "let result = add(five, ten);"
            + "if (5 < 10) {"
            + "    return true;"
            + "} else {"
            + "    return false;"
            + "}"
        );

        inLen = input.length();
        lexer = new Lexer(input);
        
        for(int i = 0; i < inLen; ++i)
        {
            token = lexer.nextToken();
            if (refVals.containsKey(token.literal))
            {
                if(!refVals.containsValue(token.type))
                {
                    System.out.println("Error: unknown type " + token.type);
                    break;
                }
                continue;
            }
            
            System.out.println("Error: unknown literal " + token.literal);
            break;
        }
    }

    private static void initHashTable(Hashtable<String, String> refVals)
    {
        refVals.put("==", Token.EQ);
        refVals.put("!=", Token.NOT_EQ);
        refVals.put("let", Token.LET);
        refVals.put("five", Token.IDENT);
        refVals.put("=", Token.ASSIGN);
        refVals.put("5", Token.INT);
        refVals.put(";", Token.SEMICOLON);

        refVals.put("let", Token.LET);
        refVals.put("ten", Token.IDENT);
        refVals.put("=", Token.ASSIGN);
        refVals.put("10", Token.INT);
        refVals.put(";", Token.SEMICOLON);

        refVals.put("", Token.EOF);
        refVals.put("add", Token.IDENT);
        refVals.put("fn", Token.FUNCTION);
        refVals.put("(", Token.LPAREN);
        refVals.put(")", Token.RPAREN);
        refVals.put("x", Token.IDENT);
        refVals.put(",", Token.COMMA);
        refVals.put("y", Token.IDENT);
        refVals.put("{", Token.LBRACE);
        refVals.put("+", Token.PLUS);
        refVals.put("}", Token.RBRACE);
        refVals.put("result", Token.IDENT);
        refVals.put("if", Token.IF);
        refVals.put("<", Token.LT);
        refVals.put("return", Token.RETURN);
        refVals.put("true", Token.TRUE);
        refVals.put("else", Token.ELSE);
        refVals.put("false", Token.FALSE);
        /*
            "let add = fn(x, y) {"
            "   x + y;"
            "};"
            "let result = add(five, ten);"
            "if (5 < 10) {"
            "    return true;"
            "} else {"
            "    return false;"
            "}"
         */

    }
}
