package com.zain.zag;

public class Return extends RuntimeException{

    Object value;
    Return(Object value){
        super(null,null, false, false);
        this.value = value;
    }
}
