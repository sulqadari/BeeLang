import ru.beeline.interpreter.repl.Repl;

public class Main
{
    public static void main(String[] args)
    {
        Repl repl = new Repl();
        repl.start(System.in, System.out);
    }    
}
