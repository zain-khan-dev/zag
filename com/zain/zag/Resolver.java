package com.zain.zag;

import com.zain.zag.Stmt.Block;
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
    public Void visitVarExpr(Expr.Variable varExpr){
        if(!scopes.isEmpty() && scopes.peek().get(varExpr.name.lexeme) == Boolean.FALSE){
            Zag.error(varExpr.name, "Cant read variable in its own initializer");
        }
        resolveLocal(varExpr);
        
        return null;
    }


    public void resolveLocal(Expr.Variable expr){
        for(int i=scopes.size()-1;i>=0;i--){
            scopes.get(i).containsKey(expr.name.lexeme);
            interpreter.resolve(expr, scopes.size()-1-i);
            return;
        }
    }

}
