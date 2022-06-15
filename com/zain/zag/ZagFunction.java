package com.zain.zag;
import java.util.List;
public class ZagFunction implements ZagCallable {

    Stmt.Function func;
    

    ZagFunction(Stmt.Function func){
        this.func = func;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object>arguments){
        Environment environment = new Environment(interpreter.global);

        for(int i=0;i<arguments.size();i++){
            environment.define(func.parameters.get(i).lexeme, arguments.get(i));
        }
        interpreter.executeBlock(func.body, environment);
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
