package com.zain.zag;

import java.util.Map;
import java.util.HashMap;


public class ZagInstance {
    private ZagClass zagClass;

    private final Map<String, Object> fields = new HashMap<>();    
    
    ZagInstance(ZagClass zagClass){
        this.zagClass = zagClass;
    }

    @Override
    public String toString() {
        return zagClass.name + " instance";
    }

    public Object get(Token name){
        if(fields.containsKey(name.lexeme)){
            return fields.get(name.lexeme);
        }
        throw new RuntimeError(name, "Undefined property" + name.lexeme + " '.");
    }

    public Object set(Token name, Object value){
        return fields.put(name.lexeme, value);
    }


}