import java.util.Hashtable;

import javax.naming.LinkException;

import ru.beeline.interpreter.exception.InterpreterException;
import ru.beeline.interpreter.lexer.Lexer;
import ru.beeline.interpreter.token.Token;
import ru.beeline.interpreter.token.TokenConstants;

public class LexerTest implements TokenConstants
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
        Hashtable<String, String> hashTable = new Hashtable<String, String>();

        initHashTable(hashTable);
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
            if (hashTable.containsKey(token.literal))
            {
                if(!hashTable.containsValue(token.type))
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

    private static void initHashTable(Hashtable<String, String> hashTable)
    {
        hashTable.put("==", EQ);
        hashTable.put("!=", NOT_EQ);
        hashTable.put("let", LET);
        hashTable.put("five", IDENT);
        hashTable.put("=", ASSIGN);
        hashTable.put("5", INT);
        hashTable.put(";", SEMICOLON);

        hashTable.put("let", LET);
        hashTable.put("ten", IDENT);
        hashTable.put("=", ASSIGN);
        hashTable.put("10", INT);
        hashTable.put(";", SEMICOLON);

        hashTable.put("", EOF);
        hashTable.put("add", IDENT);
        hashTable.put("fn", FUNCTION);
        hashTable.put("(", LPAREN);
        hashTable.put(")", RPAREN);
        hashTable.put("x", IDENT);
        hashTable.put(",", COMMA);
        hashTable.put("y", IDENT);
        hashTable.put("{", LBRACE);
        hashTable.put("+", PLUS);
        hashTable.put("}", RBRACE);
        hashTable.put("result", IDENT);
        hashTable.put("if", IF);
        hashTable.put("<", LT);
        hashTable.put("return", RETURN);
        hashTable.put("true", TRUE);
        hashTable.put("else", ELSE);
        hashTable.put("false", FALSE);
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
