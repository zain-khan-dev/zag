package com.zain.zag;

import java.util.HashMap;
import java.util.Map;


public class Environment {

    Environment enclosing;
    private final Map<String,Object>variableMapping = new HashMap<>();

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }


    public Environment ancestor(int depth){

        Environment environment = this;
        while(depth != 0){
            environment = environment.enclosing;
            depth--;
        }
        return environment;
    }


    public Object getAt(int depth, String name){
        return ancestor(depth).variableMapping.get(name);
    }


    public void assignAt(int depth, String name, Object value){
        ancestor(depth).variableMapping.put(name, value);
    }
    

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
