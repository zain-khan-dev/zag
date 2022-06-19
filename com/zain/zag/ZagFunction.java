package com.zain.zag;
import java.util.List;
public class ZagFunction implements ZagCallable {

    Stmt.Function func;

    private final Environment closure;
    private final boolean isInitializer;

    ZagFunction(Stmt.Function func, Environment environment, boolean isInitializer){
        this.func = func;
        this.closure = environment;
        this.isInitializer = isInitializer;
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
        if(isInitializer){
            return closure.getAt(0, "this");
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

    ZagFunction bind(ZagInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new ZagFunction(func, environment, isInitializer);
    }

}
