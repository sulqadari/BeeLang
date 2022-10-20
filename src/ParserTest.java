import java.util.ArrayList;

import ru.beeline.interpreter.ast.LetStatement;
import ru.beeline.interpreter.ast.ReturnStatement;
import ru.beeline.interpreter.ast.RootNode;
import ru.beeline.interpreter.ast.interfaces.StatementInterface;
import ru.beeline.interpreter.lexer.Lexer;
import ru.beeline.interpreter.parser.Parser;
import ru.beeline.interpreter.token.Token;

public class ParserTest
{
    public static void main(String[] args)
    {
        Token.initKeywords();
        //letStatementsTest();
        returnStatementsTest();
    }

    public static void returnStatementsTest()
    {
        String input =
            "return 5;"
            +"return 10;"
            +"return 838383;";
        
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        RootNode root = parser.parse();

        if (hasErrors(parser))
        {
            return;
        }

        if (null == root)
        {
            System.out.println("ERROR: RootNode is null.");
            return;
        }

        if (root.statements.size() != 3)
        {
            System.out.println("ERROR: RootNode.statements doesn't contain 3 LET statements.");
            return;
        }

        int i = 0;
        for (StatementInterface statement : root.statements)
        {
            if (!assertReturnStatement(statement))
            {
                return;
            }
        }
    }

    public static boolean assertReturnStatement(StatementInterface statement)
    {
        if (statement.getTokenLiteral().compareTo("return") != 0)
        {
            System.out.printf("statement.literal not 'return'. Got=%s", statement.getTokenLiteral());
            return false;
        }

        return true;
    }

    public static void letStatementsTest()
    {
        String input =
            "let x 5;"
            +"let = 10;"
            +"let 838383;";
            //   "let x = 5;"
            // + "let y = 10;"
            // + "let foobar = 838383;";
        
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        RootNode root = parser.parse();

        if (hasErrors(parser))
        {
            return;
        }

        if (null == root)
        {
            System.out.println("ERROR: RootNode is null.");
            return;
        }

        if (root.statements.size() != 3)
        {
            System.out.println("ERROR: RootNode.statements doesn't contain 3 LET statements.");
            return;
        }

        int i = 0;
        String[] testVals = {"x", "y", "foobar"};
        for (StatementInterface statement : root.statements)
        {
            if (!assertLetStatement(statement, testVals[i++]))
            {
                return;
            }
        }
    }

    public static boolean assertLetStatement(StatementInterface statement, String name)
    {
        if (statement.getTokenLiteral().compareTo("let") != 0)
        {
            System.out.printf("statement.literal not 'let'. Got=%s", statement.getTokenLiteral());
            return false;
        }

        if (((LetStatement)statement).name.value.compareTo(name) != 0)
        {
            System.out.printf("name.value not '%s'. Got=%s", name, ((LetStatement)statement).name.value);
            return false;
        }

        if (((LetStatement)statement).name.getTokenLiteral().compareTo(name) != 0)
        {
            System.out.printf("statement.name.literal not '%s'. Got=%s", name, ((LetStatement)statement).name.getTokenLiteral());
            return false;
        }

        return true;
    }

    public static boolean hasErrors(Parser parser)
    {
        ArrayList<String> errors = parser.getErrorsList();
        if (errors.size() == 0)
            return false;
        
        System.out.printf("Parses has %d errors:\n", errors.size());

        int i = 1;
        for (String error : errors)
        {
            System.out.printf("%2d: %2s\n", i++, error);
        }

        return true;
    }
}
