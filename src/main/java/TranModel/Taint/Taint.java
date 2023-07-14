package TranModel.Taint;

import soot.SootField;
import soot.Value;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Taint{
    public Value object;
    public List<SootField> accessPath;
    public HashSet<Taint> aliases = new HashSet<>();

    // taint-alloc 处理 new array[input.length]的情况，它应该没有new array[input] 这么大的危害
    public boolean flag_LENGTH = false;

    // 返回null表示不匹配，返回空list表示匹配到object自身的taint
    //用来找b.field会带来的污点 b本身会带来的污点很好找，因为只需要比较object就可以了
    public List<SootField> match(Value object, SootField field){
        if(object.equals(this.object)){
            if(accessPath.isEmpty()) return new LinkedList<>();
            if(accessPath.get(0).equals(field)){
                LinkedList<SootField> subList = new LinkedList<>();
                for(int ind = 1; ind < accessPath.size(); ind++) subList.add(accessPath.get(ind));
                return subList;
            }
        }
        return null;
    }
    //判断一个taint是不是自己，或者是不是自己的更细划分字段
    public boolean match(Taint a){
        if(a.object != this.object)return false;
        if(a.accessPath.size() < this.accessPath.size())return false;
        for(Integer ind = 0;ind < this.accessPath.size();++ind){
            if(!this.accessPath.get(ind).equals(a.accessPath.get(ind)))return false;
        }
        return true;
    }
    public Taint(Value object, List<SootField> accessPath){
        this.object = object;
        this.accessPath = accessPath;
    }

    public Taint(Value object){
        this.object = object;
        this.accessPath = new LinkedList<>();
    }

    @Override
    public int hashCode() {
        if(this.object == null || this.accessPath == null) return 123;
        return this.object.hashCode() + this.accessPath.size() * 113;
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof Taint)) return false;
        Taint taint = (Taint) obj;
        if((taint.object == null && this.object == null) || (taint.object != null && taint.object.equals(this.object))){
            if(this.accessPath.size() == taint.accessPath.size()){
                for(int ind = 0; ind < this.accessPath.size(); ind++){
                    if(!this.accessPath.get(ind).equals(taint.accessPath.get(ind))) return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return " [" + object + "  " + accessPath + "] ";
    }
}
