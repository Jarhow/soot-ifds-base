package TranModel.Taint;

import soot.SootMethod;
import soot.Value;

import java.util.HashSet;

public class LocalVal implements Val{
    public Value value;
    public SootMethod method;

    public enum AllocState{
        ALLOC, FREE, ERROR, ALLOC_OR_FREE
    }

    public HashSet<Val> toVals = new HashSet<>();
    public HashSet<Val> fromVals = new HashSet<>();
    public AllocState allocState;

    public LocalVal(Value value, SootMethod method) {
        this.value = value;
        this.method = method;
    }

    @Override
    public String toString(){
        return "[Value] " + value + "  [method] " + method + " [type] " + value.getType();
    }
}
