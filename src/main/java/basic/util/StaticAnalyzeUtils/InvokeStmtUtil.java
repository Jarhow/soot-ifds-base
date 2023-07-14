package basic.util.StaticAnalyzeUtils;

import basic.cfg.Node;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;

import java.util.HashSet;

/**
 * 静态分析基本能力
 */
public class InvokeStmtUtil {

    public static ValueBox getObjectValueBox(Unit unit){
        for(ValueBox valueBox : unit.getUseBoxes()){
            if(valueBox.toString().contains("JimpleLocalBox")) return valueBox;
        }
        return null;
    }

    public static ValueBox getArgValueBox(Unit unit, int argIndex){
        if(!((Stmt) unit).containsInvokeExpr()) return null;
        return  ((Stmt) unit).getInvokeExpr().getArgBox(argIndex);
    }

    public static String getDefVariableName(Node node){
        if(node.unit.getDefBoxes().isEmpty()) return null;
        return node.unit.getDefBoxes().get(0).getValue().toString();
    }

    public static HashSet<ValueBox> getAllArgValueBoxes(Unit unit){
        HashSet<ValueBox> ret = new HashSet<>();
        if(!((Stmt) unit).containsInvokeExpr()) return ret;
        for(int ind = 0 ; ind < ((Stmt) unit).getInvokeExpr().getArgCount(); ind++){
            ValueBox vb = ((Stmt) unit).getInvokeExpr().getArgBox(ind);
            ret.add(vb);
        }
        return ret;
    }

    public static int getArgIndexByValueBox(Unit unit, ValueBox valueBox){
        if(!((Stmt) unit).containsInvokeExpr()) return -1;
        for(int ind = 0 ; ind < ((Stmt) unit).getInvokeExpr().getArgCount(); ind++){
            ValueBox vb = ((Stmt) unit).getInvokeExpr().getArgBox(ind);
            if(vb.equals(valueBox)) return ind;
        }
        return -1;
    }
}
