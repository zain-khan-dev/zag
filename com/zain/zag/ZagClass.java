package com.zain.zag;
import java.util.List;
import java.util.Map;

public class ZagClass implements ZagCallable {
    final String name;
    private final Map<String,ZagFunction>methods;


    ZagClass(String name, Map<String,ZagFunction>methods){
        this.name = name;
        this.methods = methods;
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

    public ZagFunction getMethod(String name){
        if(methods.containsKey(name))
            return methods.get(name);
        return null;
    }

}


