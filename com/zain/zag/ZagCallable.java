package com.zain.zag;
import java.util.List;


interface ZagCallable {
    Object call(Interpreter interpreter, List<Object>arguments);

    int arity();
}
