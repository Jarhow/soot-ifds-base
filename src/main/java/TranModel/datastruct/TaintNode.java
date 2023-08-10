package TranModel.datastruct;

import TranModel.TransformableNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import soot.SootMethod;
import soot.Value;

import java.util.LinkedList;
import java.util.List;

public class TaintNode {

    @JsonIgnore
    public Value value;

    @JsonIgnore
    public SootMethod inMethod;

    public String getValueStr() {
        return value.toString();
    }

    public String getMethodStr() {
        return inMethod.toString();
    }

    public String getTypeStr() {
        return value.getType().toString();
    }

    public String getParamInd() {
        if(paramInd == null) {
            return "null";
        } else {
            return paramInd.toString();
        }
    }

    @JsonIgnore
    public List<TaintEdge> outs = new LinkedList<>();

    @JsonIgnore
    public List<TaintEdge> ins = new LinkedList<>();

    // 如果是方法参数对应的value，设置它
    public Integer paramInd = null;

    public TaintNode(Value va, SootMethod sootMethod) {
        value = va;
        inMethod = sootMethod;
    }

    public void addSuccessor(TaintNode outNode, TransformableNode assignNode) {
        TaintEdge edge = new TaintEdge(this, outNode, assignNode);
        this.outs.add(edge);
        outNode.ins.add(edge);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if(paramInd != null) {
            stringBuilder.append("[param-index] ").append(paramInd);
            stringBuilder.append(" || ");
        }
        stringBuilder.append("[value-type] ").append(value.getType());
        stringBuilder.append(" || ");
        stringBuilder.append("[value] ").append(value);
        stringBuilder.append(" || ");
        stringBuilder.append("[method] ").append(inMethod);
        stringBuilder.append(" || ");
        stringBuilder.append("[out-size] ").append(outs.size());
        stringBuilder.append(" || ");
        stringBuilder.append("[in-size] ").append(ins.size());
        return stringBuilder.toString();
    }

}
