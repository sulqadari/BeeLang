import java.util.HashMap;

import ru.beeline.interpreter.exception.InterpreterException;
import ru.beeline.interpreter.lexer.Lexer;
import ru.beeline.interpreter.token.Token;
import ru.beeline.interpreter.token.TokenConstants;

public class Test
{
    public static void main(String[] args)
    {
        TestNextToken();
    }
    
    
    public static void TestNextToken()
    {
        Lexer lexer;
        Token token;
        String[] map = new String[18];

        inithashMap(map);
        lexer = new Lexer("=+(){},;");

        for (int i = 0; i < map.length - 1; i += 2)
        {
            token = lexer.nextToken();
            if (!token.type.contentEquals(map[i]) || !token.literal.contentEquals(map[i+1]))
                throw new InterpreterException("Test failed");
        }
    }

    private static void inithashMap(String[] map)
    {
        map[0] = TokenConstants.ASSIGN;
        map[1] = "=";
        map[2] = TokenConstants.PLUS;
        map[3] = "+";

        map[4] = TokenConstants.LPAREN;
        map[5] = "(";
        map[6] = TokenConstants.RPAREN;
        map[7] = ")";

        map[8] = TokenConstants.LBRACE;
        map[9] = "{";
        map[10] = TokenConstants.RBRACE;
        map[11] = "}";

        map[12] = TokenConstants.COMMA;
        map[13] = ",";
        map[14] = TokenConstants.SEMICOLON;
        map[15] = ";";
        map[16] = TokenConstants.EOF;
        map[17] = " ";
    }
}
