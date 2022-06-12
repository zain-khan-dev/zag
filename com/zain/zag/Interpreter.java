package com.zain.zag;

import java.util.List;
import static com.zain.zag.TokenType.*;


public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{

    Environment environment = new Environment();


    public void interpret(List<Stmt>statements) {

        
        try{
            for(Stmt statement:statements){
                execute(statement);
            }
        }
        catch(RuntimeError error){
            Zag.runtimeError(error);
        }
        
    }


    @Override
    public Void visitVarStmt(Stmt.Var variable) {

        Object value = null;

        if(variable.initializer != null){
            value = evaluate(variable.initializer);
        }

        environment.define(variable.name.lexeme, value);

        return null;


    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return environment.get(expr.name);
    }


    public void execute(Stmt statement){

        statement.accept(this);
    }


    public String stringify(Object value){
        if(value == null)
        return "nil";
        if(value instanceof Double){ 
            String number = value.toString();
            if(number.endsWith(".0")){ // convert to number 
                number = number.substring(0, number.length()-2);
            }
            return number;
        }
        return value.toString();
    }



    @Override
    public Void visitExpressionStmt(Stmt.Expression stmtExpression) { 
        Expr expression = stmtExpression.expression;
        evaluate(expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print printExpression) {
        Object value = evaluate(printExpression.expression);
        System.out.println(stringify(value));
        return null;
    }



    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }


    Object evaluate(Expr expr){
        return expr.accept(this);
    }


    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }


    private boolean parseTruthy(Object obj){
        if(obj == null)
            return false;
        if(obj instanceof Boolean)
            return (boolean)obj;
        return true;
    }


    public void checkNumberOperand(Token operator, Object left){
        if(left instanceof Double)
        return;
        throw new RuntimeError(operator, "Operand must be a number");
    }


    public void checkNumberOperand(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double)
        return;
        throw new RuntimeError(operator, "Operands must be two number");
    }


    public Object visitUnaryExpr(Expr.Unary expr) {
        Token operator = expr.operator;
        Object right = evaluate(expr.right);

        checkNumberOperand(operator, right);
        switch(operator.type){
            case MINUS:
                return -(double)right;
            case BANG:
                return !parseTruthy(right);
        }
        return null;
    }

    private boolean isEqual(Object left, Object right){
        if(left == null && right == null)
        return true;
        if(left == null)
        return false;
        return left.equals(right);
    }

    public Object visitBinaryExpr(Expr.Binary expr){
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if(expr.operator.type != PLUS){
            checkNumberOperand(expr.operator, left, right);
        }

        switch(expr.operator.type){
            case COMMA:
                return (double)right;
            case GREATER:
                return (double)left > (double)right;
            case GREATER_EQUAL:
                return (double)left >= (double)right;
            case LESS:
                return (double)left < (double)right;
            case LESS_EQUAL:
                return (double)left <= (double)right;
            case PLUS:
                if(left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                if(left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                if(left instanceof Double && right instanceof String || right instanceof Double && left instanceof String){
                    if(left instanceof Double)
                        left = stringify(left);
                    else
                        right = stringify(right);
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two Strings or Numbers");
            case MINUS:
                return (double)left - (double)right;
            case SLASH:
                if((double)right == 0){
                    throw new RuntimeError(expr.operator, "Divide by zero error occured");
                }
                return (double)left / (double)right;
            case STAR:
                return (double)left * (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }
        return null;
    }

    
}
