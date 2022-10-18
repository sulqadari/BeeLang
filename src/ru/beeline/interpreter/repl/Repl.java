package ru.beeline.interpreter.repl;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import ru.beeline.interpreter.lexer.Lexer;
import ru.beeline.interpreter.token.Token;
import ru.beeline.interpreter.token.TokenConstants;

public class Repl
{
    private final String CONST = ">> ";
    private Lexer lexer = null;
    private Token token = null;
    
    private String input = "";
    public void start(InputStream in, PrintStream out)
    {
        Scanner scanner = new Scanner(in);
        out.print(CONST);
        while(scanner.hasNextLine())
        {
            input = scanner.nextLine();
            lexer = new Lexer(input);
            //token = lexer.nextToken();
            for (token = lexer.nextToken(); token.type != TokenConstants.EOF; token = lexer.nextToken())
            {
                out.printf("Type: %-8s Literal: %8s\n", token.type, token.literal);
            }
            // while(token.type != TokenConstants.EOF)
            // {

            // }
            out.print(CONST);
        }
        scanner.close();
    }
}