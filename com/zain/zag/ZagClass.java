package com.zain.zag;
import java.util.List;

public class ZagClass implements ZagCallable {
    final String name;

    ZagClass(String name){
        this.name = name;
    }

    
    @Override
    public String toString() {
        return this.name;
    }


    @Override
    public int arity(){
        return 0;
    }

    @Override 
    public Object call(Interpreter interpreter, List<Object> arguments){
        ZagInstance instance = new ZagInstance(this);
        return instance;
    }
}
