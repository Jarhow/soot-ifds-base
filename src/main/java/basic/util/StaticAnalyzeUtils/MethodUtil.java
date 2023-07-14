package basic.util.StaticAnalyzeUtils;

import basic.cg.CG;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.*;

/**
 * 静态分析基本能力
 */
public class MethodUtil {
    public static List<SootMethod> getCallPath(SootMethod src, SootMethod tgt, CG cg){
        Stack<SootMethod> record = new Stack<>();
        if(dfsCGFromEntry(src, tgt, cg, record, new HashSet<>(), 20)){
            return new LinkedList<>(record);
        }
        else return null;
    }

    private static boolean dfsCGFromEntry(SootMethod sootMethod, SootMethod tgt, CG cg, Stack<SootMethod> record, HashSet<SootMethod> visited, int limit){
//        System.out.println(sootMethod.getSignature());
        if(sootMethod.equals(tgt)) {
            record.push(tgt);
            return true;
        }
        if(limit == 0) return false;
        visited.add(sootMethod);
        record.push(sootMethod);
        for (Iterator<Edge> it = cg.callGraph.edgesOutOf(sootMethod); it.hasNext(); ) {
            SootMethod invokeMethod = it.next().tgt();
            if(visited.contains(invokeMethod)) continue;
            if(dfsCGFromEntry(invokeMethod, tgt, cg, record, visited, limit - 1)) {
                return true;
            }
        }
        record.pop();
        return false;
    }
    public static List<SootMethod> getCallPathToTgtSet(SootMethod src, HashSet<SootMethod> tgts, CG cg){
        Stack<SootMethod> record = new Stack<>();
        if(dfsCGFromEntryToTgtSet(src, tgts, cg, record, new HashSet<>(), 20)){
            return new LinkedList<>(record);
        }
        else return null;
    }

    private static boolean dfsCGFromEntryToTgtSet(SootMethod sootMethod, HashSet<SootMethod> tgts, CG cg, Stack<SootMethod> record, HashSet<SootMethod> visited, int limit){
        if(tgts.contains(sootMethod)){
            record.push(sootMethod);
            return true;
        }
        if(limit == 0) return false;
        visited.add(sootMethod);
        record.push(sootMethod);
        for (Iterator<Edge> it = cg.callGraph.edgesOutOf(sootMethod); it.hasNext(); ) {
            SootMethod invokeMethod = it.next().tgt();
            if(visited.contains(invokeMethod)) continue;
            if(dfsCGFromEntryToTgtSet(invokeMethod, tgts, cg, record, visited, limit - 1)) {
                return true;
            }
        }
        record.pop();
        return false;
    }

    public static HashSet<AnnotationTag> getClassAnnotations(SootClass sootClass){
        HashSet<AnnotationTag> ret = new HashSet<>();
        if(sootClass.getTags().size()==0){
            return ret;
        }

        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if (visibilityAnnotationTag !=null){
            ret.addAll(visibilityAnnotationTag.getAnnotations());
        }
        return ret;
    }

    public static HashSet<AnnotationTag> getMethodAnnotations(SootMethod sootMethod){
        HashSet<AnnotationTag> ret = new HashSet<>();
        if(sootMethod.getTags().size()==0){
            return ret;
        }

        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootMethod.getTag("VisibilityAnnotationTag");
        if (visibilityAnnotationTag !=null){
            for (AnnotationTag annotationTag:visibilityAnnotationTag.getAnnotations()){
                ret.add(annotationTag);
            }
        }
        return ret;
    }

    public static HashSet<SootMethod> allSootMethods() {
        HashSet<SootMethod> rets = new HashSet<>();
        for(SootClass sootClass : Scene.v().getApplicationClasses()) {
            rets.addAll(sootClass.getMethods());
        }
        return rets;
    }

    // 递归找最上层的方法声明
    private static SootMethod findOverrideParent_recurse(SootMethod sootMethod, SootClass sootClass, HashSet<SootClass> visited, boolean ignore_java) {

        if(ignore_java && sootClass.isJavaLibraryClass()) return sootMethod;

        visited.add(sootClass);
        SootMethod newMethod = sootMethod;
        // 如果这个类里有override， 把当前找到的方法声明设置为它
        for(SootMethod methodInParent : sootClass.getMethods()) {
            if(methodInParent.getSubSignature().equals(sootMethod.getSubSignature())) {
                newMethod = methodInParent;
            }
        }
        SootClass parentClass = sootClass.getSuperclassUnsafe();
        if(parentClass != null && !visited.contains(parentClass)) {
            newMethod = findOverrideParent_recurse(newMethod, parentClass, visited, ignore_java);
        }
        for (SootClass parentClass1 : sootClass.getInterfaces()) {
            if(visited.contains(parentClass1)) continue;
            newMethod = findOverrideParent_recurse(newMethod, parentClass1, visited, ignore_java);
        }
        return newMethod;
    }

    // 找最上层的方法声明
    public static SootMethod findOverrideAncestor(SootMethod sootMethod) {
        return findOverrideParent_recurse(sootMethod, sootMethod.getDeclaringClass(), new HashSet<>(), false);
    }

    // 找最上层的方法声明, 忽略java.方法
    public static SootMethod findOverrideAncestor_ignoreJava(SootMethod sootMethod) {
        return findOverrideParent_recurse(sootMethod, sootMethod.getDeclaringClass(), new HashSet<>(), true);
    }

    public static List<SootMethod> findOverrideParents(SootMethod sootMethod) {
        HashSet<SootMethod> results = new HashSet<>();
        findOverrideParents_recurse(sootMethod, sootMethod.getDeclaringClass(), new HashSet<>(), results);
        return new LinkedList<>(results);
    }

    private static void findOverrideParents_recurse(SootMethod sootMethod, SootClass sootClass, HashSet<SootClass> visited, HashSet<SootMethod> founds) {
        visited.add(sootClass);
        SootMethod newMethod = sootMethod;
        // 如果这个类里有override
        for(SootMethod methodInParent : sootClass.getMethods()) {
            if(methodInParent.getSubSignature().equals(sootMethod.getSubSignature())) {
                newMethod = methodInParent;
                founds.add(newMethod);
            }
        }
        SootClass parentClass = sootClass.getSuperclassUnsafe();
        if(parentClass != null && !visited.contains(parentClass)) {
            findOverrideParents_recurse(newMethod, parentClass, visited, founds);
        }
        for (SootClass parentClass1 : sootClass.getInterfaces()) {
            if(visited.contains(parentClass1)) continue;
            findOverrideParents_recurse(newMethod, parentClass1, visited, founds);
        }
        return;
    }

    public static List<SootMethod> findOverideSons(SootMethod sootMethod) {
        List<SootMethod> ret = new LinkedList<>();
        for(SootClass sootClass : ClassUtils.getAllSubs(sootMethod.getDeclaringClass())) {
            for(SootMethod sootMethod1 : sootClass.getMethods()) {
                if(sootMethod1.getSubSignature().equals(sootMethod.getSubSignature())) {
                    ret.add(sootMethod1);
                }
            }
        }
        return ret;
    }

    // 寻找重写方法的双亲/兄弟方法,不考虑java.方法
    public static List<SootMethod> findOverrideParentsAndBrothers(SootMethod sootMethod) {
        SootMethod ancestor = findOverrideAncestor_ignoreJava(sootMethod);
        return findOverideSons(ancestor);
    }

}
