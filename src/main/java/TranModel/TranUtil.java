package TranModel;

import basic.cfg.CFG;
import basic.cfg.Node;
import basic.cg.CG;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class TranUtil {
    public static CG cg;

    public static int error_cannot_topological_order = 0;

    public static void init(CG cgg){
        cg = cgg;
    }

//    class CompTran implements Comparator<TransformableNode>{
//        @Override
//        public int compare(TransformableNode o1, TransformableNode o2) {
//            return o1.
//        }
//    }

    public static boolean DEBUG = false;
    public static List<TransformableNode> getTopologicalOrderedTNodesFromCFG(CFG cfg){
        HashMap<Node, TransformableNode> nodeMapTransformNode = new HashMap<>();
        HashMap<TransformableNode, Integer> transformNodeMapPrecursorSize = new HashMap<>();
        HashSet<TransformableNode> waiting = new HashSet<>();
        for(Node node : cfg.allNodes.values()){
            // 我重写了TransformableNode 的hash方法，使得同一cfg多次执行获得的拓扑序是一致的
            TransformableNode transformableNode = new TransformableNode(node, cfg.entryPoint);
            transformNodeMapPrecursorSize.put(transformableNode, node.precursorNodes.size());
            nodeMapTransformNode.put(node, transformableNode);
            waiting.add(transformableNode);
        }
//        HashSet<TransformableNode> waiting = new HashSet<>(transformNodeMapPrecursorSize.keySet());
//        List<TransformableNode> waitingList = new LinkedList<>(waiting);
//        Collections.sort(waitingList, );

        List<TransformableNode> ret = new LinkedList<>();
        if(DEBUG){
            System.out.println("<<<<<<<<<<<<<<");
            for(TransformableNode transformableNode : waiting) System.out.println(transformableNode);
            System.out.println(">>>>>>>>>>>>>>");
        }
        int topologicalOrderFail = 0;
        while(!waiting.isEmpty()){
            int fl = 0;
            for(TransformableNode transformableNode : waiting){
                if(transformNodeMapPrecursorSize.get(transformableNode) == 0){
                    ret.add(transformableNode);
                    waiting.remove(transformableNode);
                    for(Node node : transformableNode.node.successorNodes){
                        TransformableNode successor = nodeMapTransformNode.get(node);
                        int temp = transformNodeMapPrecursorSize.get(successor);
                        transformNodeMapPrecursorSize.put(successor, temp - 1);

                        transformableNode.successors.add(successor);
                        successor.precursors.add(transformableNode);
                    }
                    fl = 1;
                    break;
                }
            }
            if(fl == 0){
//                TransformableNode transformableNode = waiting.iterator().next();
                // 找剩下结点中入度最高的：原因：循环中有环，入度最高的最可能是循环的入口
                TransformableNode transformableNode = waiting.iterator().next();
                int maxIn = 0;
                for(TransformableNode t : waiting){
                    if(t.precursors.size() > maxIn){
                        maxIn = t.precursors.size();
                        transformableNode = t;
                    }
                }
                ret.add(transformableNode);
                waiting.remove(transformableNode);
                for(Node node : transformableNode.node.successorNodes){
                    TransformableNode successor = nodeMapTransformNode.get(node);
                    int temp = transformNodeMapPrecursorSize.get(successor);
                    transformNodeMapPrecursorSize.put(successor, temp - 1);

                    transformableNode.successors.add(successor);
                    successor.precursors.add(transformableNode);
                }
                topologicalOrderFail = 1;
            }

        }
        if(topologicalOrderFail == 1) error_cannot_topological_order ++;
        return ret;
    }


}
