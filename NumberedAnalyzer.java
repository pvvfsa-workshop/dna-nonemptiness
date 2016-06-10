import java.util.*;
import java.util.stream.*;
import java.util.stream.Collectors.*;

/**
 * This class is an analyzer for numbered parity automata.
 */
public class NumberedAnalyzer {
    private Condenser numberedGraph;
	
	public NumberedAnalyzer(Numbered numbered)
    {
        numberedGraph = new Condenser();
        for (NumberedState state : numbered.states.values()) {
            Vertex vertex = new Vertex(state.id, new HashSet<>(), new Condenser());
            for (NumberedTransition transition : state.transitions.values()) {
                vertex.addEdge(transition.id, transition.transitionNumber, transition.transition);
            }
            numberedGraph.addVert(vertex);
        }
    }
	
    public boolean isEmpty()
    {
        return solve(numberedGraph, 1, numberedGraph.getMaxNum()) == null;
    }

    public String isEmptyOrWord()
    {
        Witness witness = solve(numberedGraph, 1, numberedGraph.getMaxNum());
        if (witness == null) {
            return "Empty";
        }
        else {
            // First find the finite prefix of the word -- the path from the first state to the witness
            // find the actual id of the beginning of witness (ew)
            String[] witnessHier = witness.edge.from.split("\\.");
            String realId = witnessHier[witnessHier.length - 1];
            Deque<Edge> prefixPath = findPath(numberedGraph, "Q0", realId);
            // Next find a cycle containing our witness
            Deque<Edge> suffixPath = findPath(witness.component, witness.edge.to, witness.edge.from);
            // Convert these to words
            String prefix = "";
            for (Edge edge : prefixPath) {
                prefix += edge.transition;
            }
            String suffix = "";
            for (Edge edge : suffixPath) {
                suffix += edge.transition;
            }
            String word = prefix + "(" + witness.edge.transition + suffix + ")*";
            return "NonEmpty:\n" + word;
        }
    }

    /**
     * Use BFS to find path.
     * Returns the sequence of edges (to be used to create witness word)
     */
    public Deque<Edge> findPath(Condenser graph, String from, String to) {
        Queue<String> Q = new ArrayDeque<>();
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Edge> parents = new HashMap<>();

        distances.put(from, 0);
        Q.add(from);

        while (!Q.isEmpty()) {
            String currId = Q.remove();
            Vertex curr = graph.getVert(currId);
            for (Edge edge : curr.edges) {
                String neighbor = edge.to;
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, distances.get(currId)+1);
                    parents.put(neighbor, edge);
                    Q.add(neighbor);
                }
            }
        }

        // Now we use parents to reconstruct the path
        Deque<Edge> path = new ArrayDeque<>();
        String curr = to;
        while (!curr.equals(from)) {
            Edge parentEdge = parents.get(curr);
            path.addFirst(parentEdge);
            curr = parentEdge.from;
        }

        return path;
    }

    /** Recursive function to find witnesses for the even cycle problem.
     * Returns true if there is a witness, false otherwise.
     * Taken from: http://www.cs.huji.ac.il/~ornak/publications/fossacs01.pdf
     */
    public Witness solve(Condenser g, int startNumber, int endNumber) {
        if (endNumber < startNumber) {
            return null;
        }

        int mid = (int)Math.ceil((startNumber + endNumber) / 2.0);
        // Filter out all the edges with number less than mid
        Condenser gMid = g.filterEdges(e -> e.number >= mid);
        Components components = gMid.getComponents();
        Condenser gMidCondensation = gMid.contract(components);
        if (mid % 2 != 0) {
            for (Vertex componentVertex : gMidCondensation.vertices.values()) {
                for (Vertex vertex : componentVertex.contraction.vertices.values()) {
                    for (Edge edge : vertex.edges) {
                        if (edge.number == mid) {
                            return new Witness(edge, componentVertex.contraction);
                        }
                    }
                }
            }
        }

        // We search for a witness in {mid+1, ..., endNumber}
        Witness stepDownWitness = solve(gMidCondensation.stepDown().filterEdges(e -> e.number >= mid+1), mid+1, endNumber);
        if (stepDownWitness != null) {
            return stepDownWitness;
        }
        return solve(gMidCondensation.stepUp(mid, g, components.roots), startNumber, mid-1);
    }
}
