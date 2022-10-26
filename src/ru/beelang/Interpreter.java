package ru.beelang;

public class Interpreter implements Expr.Visitor<Object>
{
    void interpret(Expr expression)
    {
        try
        {
            Object value = evaluate(expression);
            System.out.print("<< ");
            System.out.println(stringify(value));
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