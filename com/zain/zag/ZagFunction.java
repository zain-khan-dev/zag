package com.zain.zag;
import java.util.List;
public class ZagFunction implements ZagCallable {

    Stmt.Function func;

    private final Environment closure;
    

    ZagFunction(Stmt.Function func, Environment environment){
        this.func = func;
        this.closure = environment;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object>arguments){
        Environment environment = new Environment(closure);

        for(int i=0;i<arguments.size();i++){
            environment.define(func.parameters.get(i).lexeme, arguments.get(i));
        }
        try{
            interpreter.executeBlock(func.body, environment);
        }
        catch(Return returnVal){
            return returnVal.value;
        }
        return null;
    }

    @Override
    public int arity(){
        return func.parameters.size();
    }
    
    public String toString() {
        return "<fn" + func.name.lexeme + ">";
    }

}
