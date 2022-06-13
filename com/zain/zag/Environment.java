package com.zain.zag;

import java.util.HashMap;
import java.util.Map;


public class Environment {

    Environment enclosing;

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }


    private final Map<String,Object>variableMapping = new HashMap<>();



    void define(String variableName, Object value){
        variableMapping.put(variableName, value);
    }


    Object get(Token variableToken){

        if(variableMapping.containsKey(variableToken.lexeme)){
            return variableMapping.get(variableToken.lexeme);
        }
        if(enclosing != null){
            return enclosing.get(variableToken); 
        }
        throw new RuntimeError(variableToken, "undefined variable "+ variableToken.lexeme);
    }

    void assign(Token variableToken, Object value){
        if(variableMapping.containsKey(variableToken.lexeme)){
            variableMapping.put(variableToken.lexeme, value);
            return;
        }
        if(enclosing != null) {enclosing.assign(variableToken, value); return;}
        throw new RuntimeError(variableToken, "Assignment to undefined variable");
    }

}
