package com.zain.zag;

public class ZagInstance {
    private ZagClass zagClass;
    
    
    ZagInstance(ZagClass zagClass){
        this.zagClass = zagClass;
    }

    @Override
    public String toString() {
        return zagClass.name + " instance";
    }
}
