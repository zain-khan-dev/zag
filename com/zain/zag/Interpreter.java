package com.zain.zag;

import java.util.List;

import com.zain.zag.Expr.logical;
import java.util.ArrayList;


import static com.zain.zag.TokenType.*;


public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{

    final Environment global = new Environment();

    Environment environment = global;


    Interpreter() {
        global.define("clock", new ZagCallable() {

            @Override
            public int arity(){
                return 0;
            }


            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)(System.currentTimeMillis())/1000;   
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }


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

    public void executeBlock(List<Stmt>statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for(Stmt statement:statements){
                execute(statement);
            }
        }
        finally{
            this.environment = previous;
        }
    }


    public Void visitReturnStmt(Stmt.Return returnStmt){
        Object returnVal = null;
        if(returnStmt.value != null){
            returnVal = evaluate(returnStmt.value);
        }
        throw new Return(returnVal);

    }



    @Override
    public Void visitFunctionStmt(Stmt.Function func){
        ZagFunction function = new ZagFunction(func);
        environment.define(func.name.lexeme, function);
        return null;
    }


    @Override
    public Object visitlogicalExpr(Expr.logical logicalExpr){
        Object left = evaluate(logicalExpr.left);
        if(logicalExpr.operator.type == TokenType.AND){
            if(!parseTruthy(left)) return left;
        }
        if(logicalExpr.operator.type == TokenType.OR){
            if(parseTruthy(left)) {
                return left;
            }
        }
        return evaluate(logicalExpr.right);
    }



    @Override
    public Object visitCallExpr(Expr.Call funCall){
        Object funcName = evaluate(funCall.funcName);
        
        List<Object> arguments = new ArrayList<>();
        for(Expr argument: funCall.arguments){
            arguments.add(evaluate(argument));
        }
        if(!(funcName instanceof ZagCallable))
            throw new RuntimeError(funCall.paren, "Can call only classes and functions");
        ZagCallable function = (ZagCallable)funcName;
        if(arguments.size() != function.arity()){
            throw new RuntimeError(funCall.paren, "Expected" + function.arity() + " got "+ arguments.size());
        }
        return function.call(this, arguments);

    }


    @Override
    public Void visitIfStmt(Stmt.If ifStmt){
        Object conditionalResult = evaluate(ifStmt.condition);
        if(parseTruthy(conditionalResult)){
            execute(ifStmt.ifCondition);
        }
        else
        if(ifStmt.elseCondition != null)
            execute(ifStmt.elseCondition);
        return null;
    }


    public Void visitBlockStmt(Stmt.Block block){
        executeBlock(block.statements, new Environment(environment));
        return null;
    }


    public Void visitAssignExpr(Expr.Assign variable){
        Object result = evaluate(variable.value);
        environment.assign(variable.name, result);
        return null;
    }


    public Void visitWhileStmt(Stmt.While whileStmt){
        while(parseTruthy(evaluate(whileStmt.condition))){
            execute(whileStmt.body);
        }
        return null;
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
            System.out.println(expr.operator.type);
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
