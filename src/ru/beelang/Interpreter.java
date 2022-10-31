package ru.beelang;

import java.util.List;
import java.util.ArrayList;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
    /**Holds a fixed reference to the outermost global environment. */
    final Environment globals = new Environment();
    /** Changes as we enter and exit local scopes, tracks the current environment.*/
    private Environment environment = globals;

    Interpreter()
    {
        globals.define("clock", new BeeCallable() {
            @Override
            public int arity(){return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString(){return "<native function";}
        });
    }

    void interpret(List<Stmt> statements)
    {
        try
        {
            for(Stmt statement : statements)
            {
                execute(statement);
            }
        }catch(RuntimeError error)
        {
            Main.runtimeError(error);
        }
    }

    /**
     * Converts a literal tree node into a runtime value.
     */
    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr)
    {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR)
        {
            if (isTruthy(left))
                return left;
        }else
        {
            if (!isTruthy(left))
                return left;
        }
        return evaluate(expr.right);
    }

    /**
     * Evaluates Unary expressions.<p/>
     * Unlike Grouping, unary expression evaluates the operand expression,
     * then apply unary operator itself to the result of that.<p/>
     * There are two different unary expressions, identified by the type of the operator
     * token.
     * 
     */
    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evaluate(expr.right);

        switch(expr.operator.type)
        {
            case BANG:
                return (!isTruthy(right));  // logical NOT
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return (- (double)right);   // negate
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return environment.get(expr.name);
    }

    /**
     * the node retrieved as a result of using explicit parentheses in an expression.<p/>
     * A grouping node has a reference to an inner
     * node for the expression contained inside the parentheses.
     * To evaluate the grouping expression itself, we recursively
     * evaluate that subexpression and return it.
     */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type)
        {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;    // add
                
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;    // concat

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr)
    {
        // evaluate the expression for the callee.
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments)
        {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof BeeCallable))
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        
        //cast the callee to a BeeCallable
        BeeCallable function = (BeeCallable)callee;
        
        if (arguments.size() != function.arity())
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments "
                                    + "but got " + arguments.size() + ".");
        //perform the call
        return function.call(this, arguments);
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double)
            return;
    
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    
    /**
     * helper method which simply sends the expression back
     * into the interpreter’s visitor implementation
     * @param expr
     * @return
     */
    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    /**
     * Helper method analogue to evaluate() one, which
     * handles the Statements.
     * @param stmt
     */
    private void execute(Stmt stmt)
    {
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt)
    {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    /**
     * executes a list of statements in the context of a given environment.<p/>
     * To execute code within a given scope, this method updates the interpreter's
     * environment field, visits all of the statements,
     * and then restores the previous value.
     * @param statements
     * @param environment
     */
    void executeBlock(List<Stmt>statements, Environment environment)
    {
        Environment previous = this.environment;
        
        try
        {
            this.environment = environment;
            for (Stmt statement : statements)
                execute(statement);
        }finally
        {
            //restore previous environment even if an exception is thrown.
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt)
    {
        BeeFunction function = new BeeFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt)
    {
        if (isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.thenBranch);
        }else if (null != stmt.elseBranch)
        {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt)
    {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }
    
    @Override
    public Void visitReturnStmt(Stmt.Return stmt)
    {
        Object value = null;
        if (null != stmt.value)
            value = evaluate(stmt.value);
        
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt)
    {
        Object value = null;
        if (null != stmt.initializer)
        {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt)
    {
        while(isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.body);
        }
        return null;
    }

    /**
     * Evaluates the right-hand side to get the value,
     * then stores it in the named variable.<p/>
     * This method returns the assigned value because assignment
     * is an expression that can be nested inside other expressions.<p/>
     * <code>var a = 1;</code>
     * <code>print a = 2;</code>
     */
    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    /**
     * Throws BeeLang-specific runtime exception.
     * @param operator
     * @param operand
     */
    private void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double)
            return;
        
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isTruthy(Object object)
    {
        if (object == null)
            return false;
        
        if (object instanceof Boolean)
            return (boolean)object;
        
        return true;
    }
    
    /**
     * Returns true if <code>a</code> and <code>b</code> refer to the same Object.
     * @param a
     * @param b
     * @return
     */
    private boolean isEqual(Object a, Object b)
    {
        if (a == null && b == null)
            return true;
        
        if (a == null)
            return false;
    
        return a.equals(b);
    }

    private String stringify(Object object)
    {
        if (null == object)
            return "nil";
        
        if (object instanceof Double)
        {
            String text = object.toString();
            if(text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
}