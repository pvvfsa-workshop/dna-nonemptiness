import java.util.*;
import java.io.InputStream;
import java.util.regex.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Represents a deterministic numbered automaton (DNA).
 */
class Numbered {
    final Map<String, NumberedState> states = new HashMap<>();

    /**
     * Constructs an automaton from graphviz format.
     */
    public Numbered(InputStream in)
    {
        // The label on states is currently unused, it only conveys information about the original Buchi automaton
        Pattern statePattern = Pattern.compile("^\\s*(?<id>\\w+)\\s\\[label=\"(?<label>.+)\"\\]$");
        Pattern transitionPattern = Pattern.compile("^\\s+(?<startID>\\w+)\\s->\\s(?<nextID>\\w+)\\s\\[label=\"(?<character>\\w)\\[(?<number>\\d+)\\]\"\\]$");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            for (String line : (Iterable<String>)reader.lines()::iterator) {
                Matcher stateMatcher = statePattern.matcher(line);
                if (stateMatcher.matches()) {
                    String id = stateMatcher.group("id");
                    states.put(id, new NumberedState(id, id.equals("Q0")));
                    continue;
                }
                Matcher transitionMatcher = transitionPattern.matcher(line);
                if (transitionMatcher.matches()) {
                    NumberedState state = states.get(transitionMatcher.group("startID"));
                    Character nextChar = transitionMatcher.group("character").charAt(0);
                    state.transitions.put(nextChar, new NumberedTransition(transitionMatcher.group("nextID"),
                                Integer.parseInt(transitionMatcher.group("number")),
                            transitionMatcher.group("character").charAt(0)));
                }
                else {
                    System.out.println("Invalid input format:");
                    System.out.println(line);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
