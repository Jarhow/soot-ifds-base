package TranModel.Rules;

import basic.DefaultDetector.MethodDescriptor;
import TranModel.Rule;
import TranModel.Transformable;
import TranModel.TransformableNode;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JInstanceFieldRef;
import TranModel.datastruct.TaintNode;

/*
    这个数据流规则比TaintSpreadRule更粗，但是会把数据流Summary保存在MethodDescriptor里；
 */
public class TaintNodeRule implements Rule {
    @Override
    public void apply(Transformable transformable) {
        TransformableNode transformableNode = (TransformableNode) transformable;
        MethodDescriptor methodDescriptor = MethodDescriptor.getOrCreateDescriptor(transformableNode.context.method);

        process_assign(transformableNode, methodDescriptor);
        process_invoke(transformableNode, methodDescriptor);
        process_arrayAssign(transformableNode, methodDescriptor);
    }

    /*
        处理赋值语句
        a = taint, then taint a
     */
    private void process_assign(TransformableNode transformableNode, MethodDescriptor methodDescriptor) {

        Value from = null;
        Value to = null;

        Stmt stmt = (Stmt) transformableNode.node.unit;
        if (stmt instanceof AssignStmt) {
            Value left = ((AssignStmt) stmt).getLeftOp();
            Value right = ((AssignStmt) stmt).getRightOp();
            //处理强制类型转换的情况
            if (right instanceof CastExpr) {
                CastExpr castExpr = (CastExpr) right;
                right = castExpr.getOp();
            }

            if(right instanceof JInstanceFieldRef){
                // a = taint.field
                JInstanceFieldRef fieldRef = (JInstanceFieldRef) right;
                Value object = fieldRef.getBase();
                from = object;
                to = left;
            } else if(left instanceof  JInstanceFieldRef){
                // a.field = taint
                JInstanceFieldRef fieldRef = (JInstanceFieldRef) left;
                Value object = fieldRef.getBase();
                from = right;
                to = object;
            } else {
                from = right;
                to = left;
            }
        }

        taintSpread(from, to, methodDescriptor, transformableNode);
    }

    private void process_invoke(TransformableNode transformableNode, MethodDescriptor methodDescriptor) {
        Stmt stmt = (Stmt) transformableNode.node.unit;
        if (stmt instanceof AssignStmt){
            if(transformableNode.containsInvoke()) {
                Value to = ((AssignStmt) stmt).getLeftOp();
                for(ValueBox valueBox : transformableNode.node.unit.getUseBoxes()) {
                    Value from = valueBox.getValue();
                    taintSpread(from, to, methodDescriptor, transformableNode);
                }
            }
        }
    }

    /*
        a = taint[i] then taint a
        a[i] = taint then taint a
     */
    private void process_arrayAssign(TransformableNode transformableNode, MethodDescriptor methodDescriptor){
        Stmt stmt = (Stmt) transformableNode.node.unit;
        if (stmt instanceof AssignStmt) {
            Value left = ((AssignStmt) stmt).getLeftOp();
            Value right = ((AssignStmt) stmt).getRightOp();
            Value from = right;
            Value to = left;
            if(left instanceof JArrayRef){
                to = ((JArrayRef) left).getBase();
            }
            if(right instanceof JArrayRef){
                from = ((JArrayRef) right).getBase();
            }
            taintSpread(from, to, methodDescriptor, transformableNode);
        }
    }

    /*
        仅就Value，判断是否需要，如果需要，传播taintNode, 记录taintEdge
     */
    private void taintSpread(Value from, Value to, MethodDescriptor methodDescriptor, TransformableNode transformableNode) {
        TaintNode taintNode = methodDescriptor.getTaintNodeByValue(from);
        if(taintNode != null) {
            if(methodDescriptor.getTaintNodeByValue(to) == null) {
                TaintNode newTaintNode = new TaintNode(to, methodDescriptor.sootMethod);
                taintNode.addSuccessor(newTaintNode, transformableNode);
                methodDescriptor.addTaintNode(newTaintNode);
            }
        }
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
