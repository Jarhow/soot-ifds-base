package basic.DefaultDetector;

import TranModel.Taint.LocalVal;
import TranModel.Taint.Taint;
import basic.cfg.CFG;
import basic.dataflow.Event;
import soot.*;
import basic.util.Util;
import TranModel.datastruct.TaintNode;

import java.util.*;

//import static Detector.DEBUG_METHOD;


public class MethodDescriptor extends DefaultMethodDescriptor {

    public CFG cfg = null;

    //////////////////////////////// 23-7-20 TaintNode -sft ////////////////////////
    // TaintNode是对Taint更粗的表示，TaintNode 总结了方法内的数据链路，每个TransformableNode里都保存了自己的0到多个TaintNode
    public List<TaintNode> allTaintNodes = new LinkedList<>();
    private HashMap<Value, TaintNode> val_m_taintNode = new HashMap<>();
    public void addTaintNode(TaintNode taintNode) {
        val_m_taintNode.put(taintNode.value, taintNode);
        allTaintNodes.add(taintNode);
    }

    public TaintNode getTaintNodeByValue(Value value) {
        if(!val_m_taintNode.containsKey(value)) return null;
        return val_m_taintNode.get(value);
    }

    public int getTaintNodeSize() {
        return allTaintNodes.size();
    }

    /*
        查询输入的污点参数
     */
    public List<Integer> getInputTaintParamId() {
        List<Integer> ret = new LinkedList<>();
        for(TaintNode taintNode : allTaintNodes) {
            if(taintNode.paramInd != null) {
                ret.add(taintNode.paramInd);
            }
        }
        return ret;
    }

    //////////////////////////////// 是否需要重新检测 ////////////////////////


    ////////////////////////////////////////////////////////

    public HashMap<Integer, State> paramMapRisky = new HashMap<>();
    public HashMap<Integer, List<Event>> paramMapTaintedEvent = new HashMap<>();
    public HashMap<Value, Integer> valMapRefParam = new HashMap<>();

    public boolean isRisky = false;

    public HashSet<Unit> sinkUnits = new HashSet<>();
    public HashSet<Value> riskyValues = new HashSet<>();
    public HashMap<Integer, Value> paramIdMapLocalValue = null;

    // point-to analyze
    //记录每一个value对应的类型？这样指向分析的时候可以根据类型筛选掉一部分不准确的方法？
    public HashMap<Value, ValueInfo> valueMapInfo = new HashMap<>();

    // recurse rule
    public boolean recursive = false;
    public boolean recursive_risky = false;
    public HashSet<Value> risky_values = new HashSet<>();
    //////////////////////////////// 数据流 ////////////////////////

    public HashSet<Value> longLifeCycleValues = new HashSet<>();
    public HashSet<Taint> taints = new HashSet<>();
    //在当前检测过程中新增的taints 原来是全部加在taints里了
    public HashSet<Taint> newtaints = new HashSet<>();

    // hack： 处理掉太多太长的taints
    public void filterTaints(){
        HashSet<Taint> filtered = new HashSet<>();
        for(Taint taint : taints){
            if(taint.accessPath.size() <= 3) filtered.add(taint);
        }
        taints = filtered;
    }

    public HashSet<Taint> allTaints = new HashSet<>(); // 维护创建出的所有Taint， 其中的一些可能没有被实际污染（别名分析），实际污染的在taint里

    public Taint getOrCreateTaint(Value object, List<SootField> accessPath){

        if(object == null){
            return new Taint(null, accessPath);
        }

        if(accessPath == null){
            for(Taint taint : allTaints){
                if(taint.object.equals(object)){
                    if(taint.accessPath.isEmpty()){
                        return taint;
                    }
                }
            }
            Taint taint = new Taint(object);
            allTaints.add(taint);
            return taint;
        } else{
            for(Taint taint : allTaints){
                if(taint.object.equals(object)){
                    if(Util.listEqual(accessPath, taint.accessPath)){
                        return taint;
                    }
                }
            }
            Taint taint = new Taint(object, accessPath);
            allTaints.add(taint);
            return taint;
        }

    }



    //////////////////////////////// call frame message ////////////////////////
    // output
    public HashSet<Integer> paramBeMadeLongLife = new HashSet<>();
    public HashMap<Integer, List<Taint>> paramBeTainted = new HashMap<>();
    public HashSet<Integer> paramBeMadeExtens = new HashSet<>();
    public boolean retLongLife = false;
    public List<Taint> retTainted = new LinkedList<>();

    // input
    public HashSet<Integer> inputParamLongLife = new HashSet<>();
    public HashMap<Integer, List<Taint>> inputParamMapTaints = new HashMap<>();

    public HashSet<Integer> inputParamExtens = new HashSet<>();

    //记录传入变量的type
    public HashMap<Integer, ValueInfo> paramValInfo = new HashMap<>();

    public LocalVal retAlloc = null;


    //////////////////////////////////  记录  /////////////////////////////////////////////

    public boolean taintAlloc = false;
    public String taintAllocSinkUnit = null;
    public List<SootMethod> taintAllocStack;

    public boolean longLifeExtend = false;
    public Unit extendUnit = null;

    public void flushStates(){
        allTaints = new HashSet<>();
        taints = new HashSet<>();
        newtaints = new HashSet<>();
        longLifeCycleValues = new HashSet<>();
        paramIdMapLocalValue = new HashMap<>();

        paramBeMadeLongLife = new HashSet<>();
        paramBeTainted = new HashMap<>();
        paramBeMadeExtens = new HashSet<>();
        retLongLife = false;
        retTainted = new LinkedList<>();

        retExtens = false;

        //应该增加ValueMapInfo和ParamMapInfo?但是好像所有的都没有加，可能是由于每一回都会完全覆盖的原因(下一回的参数情况会完全覆盖上一回的)


    }
    public MethodDescriptor(SootMethod method) {
        super(method);
    }

    public List<Taint> getAllTaintsAboutThisValue(Value object){
        List<Taint> taintsForValue = new LinkedList<>();
        for(Taint taint : this.taints){
            if(taint.object.equals(object)){
                taintsForValue.add(taint);
            }
        }
        return taintsForValue;
    }

    //获得所有有关这个Value的新增的taint，排除初始传入的taint
    public List<Taint> getAllNewTaintsAboutThisValue(Value object){
        List<Taint> taintsForValue = new LinkedList<>();
        for(Taint taint : this.newtaints){
            if(taint.object.equals(object)){
                taintsForValue.add(taint);
            }
        }
        return taintsForValue;
    }
    public static HashMap<SootMethod, MethodDescriptor> methodMapDescriptor = new HashMap<>();
    public static MethodDescriptor getOrCreateDescriptor(SootMethod sootMethod){
        if(methodMapDescriptor.containsKey(sootMethod)){
            return methodMapDescriptor.get(sootMethod);
        }else{
            MethodDescriptor methodDescriptor = new MethodDescriptor(sootMethod);
            methodMapDescriptor.put(sootMethod, methodDescriptor);
            return methodDescriptor;
        }
    }

    /*
        刷新所有MethodDescriptor
     */
    public static void flushAllDescriptors() {
        methodMapDescriptor = new HashMap<>();
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
