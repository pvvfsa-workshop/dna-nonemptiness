import java.util.*;
import java.util.stream.*;

/**
 * Represents a graph vertex + edges, and may also contain a graph.
 */
class Vertex
{
    final String id;
    final Set<Edge> edges;
    final Condenser contraction;

    public Vertex(String id, Set<Edge> edges, Condenser contraction) {
        this.id = id;
        this.edges = edges;
        this.contraction = contraction;
    }

    public Vertex(String id) {
        this.id = id;
        this.edges = new HashSet<>();
        this.contraction = new Condenser();
    }

    /**
     * Adds a new outgoing edge.
     */
    public Edge addEdge(String to, int number, Character transition) {
        Edge newEdge = new Edge(this.id, to, number, transition);
        edges.add(newEdge);
        return newEdge;
    }

    public String toString() {
        return id + ":" + edges + (contraction.isEmpty() ? "" : ";" + contraction);
    }
}
