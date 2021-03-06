package com.zain.zag;
import java.util.List;
import java.util.Map;

public class ZagClass implements ZagCallable {
    final String name;
    private final Map<String,ZagFunction>methods;
    final private ZagClass superclass;

    ZagClass(String name, ZagClass superclass, Map<String,ZagFunction>methods){
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }
    
    @Override
    public String toString() {
        return this.name;
    }


    @Override
    public int arity(){
        ZagFunction initializer = findMethod("init");
        if(initializer != null){
            return initializer.arity();
        }
        return 0;
    }

    public ZagFunction findMethod(String functionName){
        if(methods.containsKey(functionName))
            return methods.get(functionName);
        if(superclass != null){
            return superclass.findMethod(functionName);
        }
        return null;
    }

    @Override 
    public Object call(Interpreter interpreter, List<Object> arguments){
        ZagInstance instance = new ZagInstance(this);

        ZagFunction initializer = findMethod("init");
        if(initializer != null){
            initializer.bind(instance).call(interpreter, arguments);
        }
        
        return instance;
    }

    public ZagFunction getMethod(String name){
        if(methods.containsKey(name))
            return methods.get(name);
        return null;
    }

}


