package TranModel;

import TranModel.Taint.LocalVal;
import TranModel.Taint.Val;
import basic.cfg.Node;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TransformableNode extends Transformable{

    // 不要用static块来实例化规则！

    public Node node;
    public SootMethod method;
    public HashSet<TransformableNode> successors = new HashSet<>();
    public HashSet<TransformableNode> precursors = new HashSet<>();

    public Context context = new Context();

    public int[] ruleFlag = new int[10];

    ////////////////////////////////////////  情景相关  /////////////////////////////////////////////
    public HashMap<Val, LocalVal.AllocState> localAllocStateMap = new HashMap<>();
    public HashSet<SootMethod> preciseInvokeMethods = new HashSet<>();


    public TransformableNode(Node node, SootMethod sootMethod){
        this.node = node;
        this.method = sootMethod;
        this.context.method = sootMethod;
        this.context.sootClass = sootMethod.getDeclaringClass();
    }

    public TransformableNode(Node node){
        this.node = node;
    }

    public TransformableNode(){
    }

    public boolean containsInvoke(){
        return ((Stmt) node.unit).containsInvokeExpr();
    }

    public HashSet<SootMethod> invokeMethods(){
        HashSet<SootMethod> ret = new HashSet<>();
        if(!containsInvoke()) return ret;
        Iterator<Edge> edgeIterator = TranUtil.cg.callGraph.edgesOutOf(node.unit);
        while (edgeIterator.hasNext()) {
            SootMethod invokeMethod = edgeIterator.next().tgt();
            ret.add(invokeMethod);
        }
        return ret;
    }

    @Override
    public String toString(){
        return this.node.unit.toString();
    }

    @Override
    public int hashCode() {
        return this.node.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof TransformableNode)) return false;
        return this.node.equals(((TransformableNode) obj).node);
    }

}
