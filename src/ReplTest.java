import ru.beeline.interpreter.repl.Repl;

public class ReplTest
{
    public static void main(String[] args)
    {
        Repl repl = new Repl();
        
        repl.start(System.in, System.out);
    }    
}
