package TranModel;

import java.util.ArrayList;
import java.util.List;

public class Transformable {
    public enum State{
        ORIGIN, TRANSFORMED, TERMINATED
    }

    static List<Rule> rules = new ArrayList<>();

    public State state = State.ORIGIN;

    public static void clearRules(){
        rules = new ArrayList<>();
    }

    public static void addRule(Rule rule){
        rules.add(rule);
    }

    public static Rule getRule(Class ruleClass){
        for(Rule rule : rules){
            if(ruleClass.equals(rule.getClass())) return rule;
        }
        return null;
    }

    public void forward(){
        state = State.TRANSFORMED;
        for(Rule rule : rules){
            rule.apply(this);
        }
    }

    public void forwardAfterRecurse(){
        for(Rule rule : rules){
            rule.applyAfterRecurse(this);
        }
    }

    public void terminate(){
        state = State.TERMINATED;
    }

    public boolean isTerminated(){
        return state.equals(State.TERMINATED);
    }
}
