package TranModel.datastruct;

import TranModel.TransformableNode;

public class TaintEdge {
    public TransformableNode transformableNode;
    public TaintNode from;
    public TaintNode to;

    public TaintEdge(TaintNode fr, TaintNode t, TransformableNode tNode){
        transformableNode = tNode;
        from = fr;
        to = t;
    }
}
