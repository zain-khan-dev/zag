package com.zain.zag;

import com.zain.zag.Stmt.Block;
import com.zain.zag.Stmt.Expression;
import com.zain.zag.Stmt.Visitor;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void>{
    
    private final Interpreter interpreter;

    private final Stack<Map<String,Boolean>> scopes = new Stack<>();

    Resolver( Interpreter interpreter){
        this.interpreter = interpreter;
    }


    private void beginScope() {

        scopes.push(new HashMap<String, Boolean>());

    }

    private void endScope() {
        scopes.pop();
    }



    public void resolve(Stmt statement) {

        statement.accept(this);
    }


    public void resolve(Expr expression){
        expression.accept(this);
    }


    public void resolve(List<Stmt> statements){
        for(Stmt statement:statements){
            resolve(statement);
        }
    }    

    public void declare(Token token) {
        if(scopes.empty()) return;
        Map<String, Boolean> context = scopes.peek();
        context.put(token.lexeme, false);
    }

    public void define(Token token) {
        if(scopes.empty()) return;
        Map<String, Boolean> context = scopes.peek();
        context.put(token.lexeme, true);

    }



    @Override
    public Void visitBlockStmt(Stmt.Block block){
        beginScope();
        resolve(block.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {
        
        declare(varStmt.name);
        if(varStmt.initializer != null)
            resolve(varStmt.initializer);

        define(varStmt.name);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable varExpr){
        if(!scopes.isEmpty() && scopes.peek().get(varExpr.name.lexeme) == Boolean.FALSE){
            Zag.error(varExpr.name, "Cant read variable in its own initializer");
        }
        resolveLocal(varExpr, varExpr.name);
        
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal literalExpr){
        return null;
    }



    @Override
    public Void visitAssignExpr(Expr.Assign assignExpr) {

        resolve(assignExpr.value);
        resolveLocal(assignExpr, assignExpr.name);
        return null;
    }


    @Override
    public Void visitFunctionStmt(Stmt.Function function){
        declare(function.name);
        define(function.name);
        resolveFunction(function);
        resolve(function.body);
        return null;
    }

//     The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitlogicalExpr(Expr.logical)Java(67109264)
// The type Resolver must implement the inherited abstract method Stmt.Visitor<Void>.visitReturnStmt(Stmt.Return)Java(67109264)
// The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitUnaryExpr(Expr.Unary)Java(67109264)
// The type Resolver must implement the inherited abstract method Stmt.Visitor<Void>.visitPrintStmt(Stmt.Print)Java(67109264)
// The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitVariableExpr(Expr.Variable)Java(67109264)
// The type Resolver must implement the inherited abstract method Stmt.Visitor<Void>.visitIfStmt(Stmt.If)Java(67109264)
// The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitCallExpr(Expr.Call)Java(67109264)
// The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitBinaryExpr(Expr.Binary)Java(67109264)
// The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitGroupingExpr(Expr.Grouping)Java(67109264)
// The type Resolver must implement the inherited abstract method Stmt.Visitor<Void>.visitWhileStmt(Stmt.While)Java(67109264)
// The type Resolver must implement the inherited abstract method Stmt.Visitor<Void>.visitExpressionStmt(Stmt.Expression)Java(67109264)
// The type Resolver must implement the inherited abstract method Expr.Visitor<Void>.visitLiteralExpr(Expr.Literal)Java(67109264)

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt);    
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        resolve(stmt.condition);
        resolve(stmt.ifCondition);
        if(stmt.elseCondition != null)
            resolve(stmt.elseCondition);
        return null;
    }


    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if(stmt.value != null)
            resolve(stmt.value);
        return null;   
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);

        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary binary){
        resolve(binary.left);
        resolve(binary.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr){
        resolve(expr.funcName);
        for(Expr args:expr.arguments){
            resolve(args);
        }
        return null;
    }

    @Override 
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitlogicalExpr(Expr.logical expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    public void resolveFunction(Stmt.Function function){
        beginScope();
        for(Token param:function.parameters){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
    }


    public void resolveLocal(Expr expr, Token name){
        for(int i=scopes.size()-1;i>=0;i--){
            scopes.get(i).containsKey(name.lexeme);
            interpreter.resolve(expr, scopes.size()-1-i);
            return;
        }
    }

}
