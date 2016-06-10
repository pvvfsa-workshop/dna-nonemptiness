import java.util.*;

class NumberedState {
    public final String id;
    public final boolean isInitial;
    public final Map<Character, NumberedTransition> transitions;

    public NumberedState(String id, boolean isInitial) {
        this.id = id;
        this.isInitial = isInitial;
        transitions = new HashMap<>();
    }

    /**
     * States are equal if they have the same id.
     * ids should be unique
     */
    public boolean equals(NumberedState other) {
        return this.id.equals(other.id);
    }
}
