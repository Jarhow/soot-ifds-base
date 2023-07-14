package basic.util.StaticAnalyzeUtils;

import basic.cfg.CFG;
import basic.cfg.Node;
import basic.dataflow.DataFlow;
import basic.dataflow.Event;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;

import java.util.*;

/**
 * 静态分析基本能力
 */
public class Parameter {

    /**
     *  返回值中 -1 : this对应的value
     */
    public static HashMap<Integer, Value> getParametersLocalValues(CFG cfg){
        HashMap<Integer, Value> paramMapValue = new HashMap<>();
        for(Node node : cfg.allNodes.values()){
            Integer paramInd = tryGetParamIdentifiedInUnit(node.unit);
            if(paramInd != null) {
                paramMapValue.put(paramInd, node.unit.getDefBoxes().get(0).getValue());
            }
        }
        return paramMapValue;
    }
    public static HashMap<Integer, List<Event>> getParametersMapTaintedEvent(CFG cfg, SootMethod sootMethod){
        int paramCount = sootMethod.getParameterCount();
        HashMap<Integer, List<Event>> paramMapTaintedEvent = new HashMap<>();
        Node node = cfg.headNode;
        for(int i = 0; i < paramCount + 1; i++){
//            System.out.println("---------------------------");
//            System.out.println(node.unit);
            Integer paramInd = tryGetParamIdentifiedInUnit(node.unit);
            if(paramInd == null) {
                if(node.successorNodes.size() == 0) break;
                node = node.successorNodes.iterator().next();
                continue;
            }
            if(paramInd == -1) {
                // "this"
                if(node.successorNodes.size() == 0) break;
                node = node.successorNodes.iterator().next();
                continue;
            }
            // affected
            ArrayList<Event> taintedEvent = new ArrayList<>();
            HashMap<Node, ValueBox> tmp = new HashMap<>();
            tmp.put(node, node.unit.getDefBoxes().get(0));
            for(Map.Entry<Node, ValueBox> entry : DataFlow.findAllUnitAndValueAffected(tmp).entrySet()){
//                System.out.println(entry.getKey());
//                System.out.println(entry.getValue());
                Event event = new Event(entry.getKey(), entry.getValue());
                taintedEvent.add(event);
            }
            // 直接引用
//            List<Event> taintedEvent = DataFlow.getValueBoxRefEvents(node.unit.getDefBoxes().get(0), node);
            paramMapTaintedEvent.put(paramInd, taintedEvent);
            if(node.successorNodes.size() == 0) break;
            node = node.successorNodes.iterator().next();
        }
        return paramMapTaintedEvent;
    }

    public static HashMap<Integer, List<Event>> getParametersMapDirectRefEvent(CFG cfg, SootMethod sootMethod){
        HashMap<Integer, List<Event>> paramMapTaintedEvent = new HashMap<>();
        for(Node node : cfg.allNodes.values()){
            Integer paramInd = tryGetParamIdentifiedInUnit(node.unit);
            if(paramInd != null) {
                List<Event> taintedEvent = DataFlow.getValueBoxRefEvents(node.unit.getDefBoxes().get(0), node);
                paramMapTaintedEvent.put(paramInd, taintedEvent);
            }

        }
        return paramMapTaintedEvent;
    }

    public static Integer tryGetParamIdentifiedInUnit(Unit unit){
        /* return:
              -1 : this
         */
        if(unit instanceof IdentityStmt){
            if(unit.getUseBoxes().size() > 0){
                String s = unit.getUseBoxes().get(0).getValue().toString();
                String[] sp = s.split("[@:]");
                if(sp.length >= 3 && sp[1].contains("parameter")){
                    try{
                        return Integer.parseInt(sp[1].substring(sp[1].length() - 1));
                    } catch (Exception e){
                        return null;
                    }
                }
                else if(sp.length >= 3 && sp[1].contains("this")) return -1;
            }
        }
        return null;
    }

    public static ValueBox getArgValueBox(Node node, int argIndex){
        if(!((Stmt) node.unit).containsInvokeExpr()) return null;
        return  ((Stmt) node.unit).getInvokeExpr().getArgBox(argIndex);
    }

    public static HashSet<ValueBox> getAllArgValueBoxes(Node node){
        HashSet<ValueBox> ret = new HashSet<>();
        if(!((Stmt) node.unit).containsInvokeExpr()) return ret;
        for(int ind = 0 ; ind < ((Stmt) node.unit).getInvokeExpr().getArgCount(); ind++){
            ValueBox vb = ((Stmt) node.unit).getInvokeExpr().getArgBox(ind);
            ret.add(vb);
        }
        return ret;
    }

    public static int getArgIndexByValueBox(Node node, ValueBox valueBox){
        if(!((Stmt) node.unit).containsInvokeExpr()) return -1;
        for(int ind = 0 ; ind < ((Stmt) node.unit).getInvokeExpr().getArgCount(); ind++){
            ValueBox vb = ((Stmt) node.unit).getInvokeExpr().getArgBox(ind);
            if(vb.equals(valueBox)) return ind;
        }
        return -1;
    }

    public static Node getParamTransferNode(CFG cfg, SootMethod sootMethod, int index){
        int paramCount = sootMethod.getParameterCount();
        Node node = cfg.headNode;
        for(int i = 0; i < paramCount + 1; i++){
//            System.out.println("---------------------------");
//            System.out.println(node.unit);
            Integer paramInd = tryGetParamIdentifiedInUnit(node.unit);
            if(paramInd == null) {
                if(node.successorNodes.size() == 0) break;
                node = node.successorNodes.iterator().next();
                continue;
            }
            if(paramInd == index) {
                return node;
            }

            if(node.successorNodes.size() == 0) break;
            node = node.successorNodes.iterator().next();
        }
        return null;
    }

    // eg. O.method->O
    public static ValueBox getThisValueBox(Node node){
        InvokeExpr invokeExpr = ((Stmt) node.unit).getInvokeExpr();
        if(invokeExpr instanceof VirtualInvokeExpr || invokeExpr instanceof InterfaceInvokeExpr) {
            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
            return instanceInvokeExpr.getBaseBox();
        }
        // hack
        ValueBox thisBox = null;
        HashSet<ValueBox> argBoxes = getAllArgValueBoxes(node);
        for (ValueBox valueBox:invokeExpr.getUseBoxes()){
            if(!argBoxes.contains(valueBox)) thisBox = valueBox;
        }
        return thisBox;
    }

    // eg. O.method->O
    public static ValueBox getThisValueBox(Stmt stmt){
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        if(invokeExpr instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
            return instanceInvokeExpr.getBaseBox();
        }
        return null;
    }

}
