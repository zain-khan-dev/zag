package com.zain.zag;

import java.util.HashMap;
import java.util.Map;


public class Environment {
    
    private final Map<String,Object>variableMapping = new HashMap<>();



    void define(String variableName, Object value){

        variableMapping.put(variableName, value);
    }


    Object get(Token variableToken){
        if(variableMapping.containsKey(variableToken.lexeme)){
            return variableMapping.get(variableToken.lexeme);
        }

        throw new RuntimeError(variableToken, "undefined variable "+ variableToken.lexeme);
    }
}
