package TranModel.Rules;

import TranModel.Rule;
import TranModel.Taint.LocalVal;
import TranModel.Taint.Val;
import TranModel.Transformable;
import TranModel.TransformableNode;

public class JoinRule implements Rule {
    @Override
    public void apply(Transformable transformable) {
        TransformableNode transformableNode = (TransformableNode) transformable;

        if(transformableNode.precursors.size() == 0) {
            return;
        }

        for(TransformableNode precursor : transformableNode.precursors){
            for(Val val : precursor.localAllocStateMap.keySet()) {
                LocalVal.AllocState allocState = precursor.localAllocStateMap.get(val);
                if(transformableNode.localAllocStateMap.containsKey(val)) {
                    LocalVal.AllocState originState = transformableNode.localAllocStateMap.get(val);
                    if(originState == LocalVal.AllocState.ALLOC && allocState == LocalVal.AllocState.ALLOC) {
                        transformableNode.localAllocStateMap.put(val, LocalVal.AllocState.ALLOC);
                    }
                    if(originState == LocalVal.AllocState.FREE && allocState == LocalVal.AllocState.ALLOC) {
                        transformableNode.localAllocStateMap.put(val, LocalVal.AllocState.ALLOC_OR_FREE);
                    }
                    if(originState == LocalVal.AllocState.ALLOC && allocState == LocalVal.AllocState.FREE) {
                        transformableNode.localAllocStateMap.put(val, LocalVal.AllocState.ALLOC_OR_FREE);
                    }
                    if(originState == LocalVal.AllocState.ALLOC_OR_FREE || allocState == LocalVal.AllocState.ALLOC_OR_FREE) {
                        transformableNode.localAllocStateMap.put(val, LocalVal.AllocState.ALLOC_OR_FREE);
                    }
                    if(originState == LocalVal.AllocState.FREE && allocState == LocalVal.AllocState.FREE) {
                        transformableNode.localAllocStateMap.put(val, LocalVal.AllocState.FREE);
                    }
                } else {
                    transformableNode.localAllocStateMap.put(val, allocState);
                }
            }
        }

//        DefaultDetector.MethodDescriptor methodDescriptor = DefaultDetector.MethodDescriptor.getOrCreateDescriptor(transformableNode.context.method);
//        if(methodDescriptor.sootMethod.getSignature().equals(Detector_v2.DEBUG_METHOD)){
//            System.out.println("join rule " + methodDescriptor.taints.size());
//        }
    }
    @Override
    public String getName() {
        return "JoinRule";
    }
}
