package TranModel;

public interface Rule {
    void apply(Transformable transformable);

    /**
     * 过程间分析递归调用后，调用此方法
     *      apply
     *      recurse
     *      applyAfterRecurse
     *
     */
    default void applyAfterRecurse(Transformable transformable) {
    }
}
