package basic.common;

import basic.DefaultDetector.DefaultMethodDescriptor;
import basic.cfg.CFG;
import basic.dataflow.Event;
import soot.*;
import basic.util.Util;

import java.util.*;


public class MethodDescriptor extends DefaultMethodDescriptor {

    public CFG cfg = null;

    //////////////////////////////// 是否需要重新检测 ////////////////////////
    public boolean visited = false;

    public HashSet<Integer> canBeTaintedParam = new HashSet<>();
    public Unit riskyAddUnit = null;

    public static HashMap<SootMethod, MethodDescriptor> methodMapDescriptor = new HashMap<>();

    public MethodDescriptor(SootMethod method) {
        super(method);
    }

    public static MethodDescriptor getOrCreateDescriptor(SootMethod sootMethod){
        if(methodMapDescriptor.containsKey(sootMethod)){
            return methodMapDescriptor.get(sootMethod);
        }else{
            MethodDescriptor methodDescriptor = new MethodDescriptor(sootMethod);
            methodMapDescriptor.put(sootMethod, methodDescriptor);
            return methodDescriptor;
        }
    }

    public static class ValueInfo{
        public Type type;
        public ValueInfo(Type t){
            type = t;
        }
        public ValueInfo(){}
    }

    //这里用来记录一些被长生命周期集合类扩增的Value
    public Set<Value> AppendValue = new HashSet<>();
    public boolean retExtens;

}
