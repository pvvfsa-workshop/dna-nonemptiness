import java.util.*;
import java.util.stream.*;

/**
 * Represents a graph in which each node may also contain a graph.
 * This allows us to comfortably represent contractions and condensations.
 */
class Condenser
{
    final Map<String, Vertex> vertices = new HashMap<>();

    public Vertex getVert(String id) {
        return vertices.get(id);
    }

    public Vertex addVert(Vertex vertex) {
        vertices.put(vertex.id, vertex);
        return vertex;
    }

    /**
     * Contracts every maximal strongly-connected component into a single vertex.
     * Uses Kosaraju's algorithm.
     */
    public Condenser condensation() {
        return contract(getComponents());
    }

    public Components getComponents() {
        Deque<Vertex> stack = new ArrayDeque<>();
        Condenser transpose = this.transpose();
        Components components = new Components();
        for (Vertex vertex : vertices.values()) {
            visit(vertex, stack, components.roots);
        }

        for (Vertex vertex : stack) {
            assign(vertex, vertex, components, transpose);
        }

        return components;
    }

    /**
     * Helper function for condensation, does dfs and adds to stack in post-order.
     */
    private void visit(Vertex vertex, Deque<Vertex> stack, Map<String, String> roots) {
        if (!roots.containsKey(vertex.id)) {
            roots.put(vertex.id, null);
            for (Edge edge : vertex.edges) {
                visit(getVert(edge.to), stack, roots);
            }
            stack.push(vertex);
        }
    }

    /**
     * Helper function for condensation, does dfs over transpose and creates the components.
     */
    private void assign(Vertex vertex, Vertex root, Components components, Condenser transpose) {
        if (components.roots.get(vertex.id) == null) {
            components.roots.put(vertex.id, root.id);
            if (!components.components.containsKey(root.id)) {
                components.components.put(root.id, new HashSet<>());
            }
            components.components.get(root.id).add(vertex);
            for (Edge inEdge : transpose.getVert(vertex.id).edges) {
                assign(getVert(inEdge.to), root, components, transpose);
            }
        }
    }

    /**
     * Returns a transposition of this graph (every edge is reversed).
     */
    public Condenser transpose() {
        Condenser transpose = new Condenser();
        vertices.keySet().stream().forEach(id -> transpose.addVert(new Vertex(id)));
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : vertex.edges) {
                transpose.getVert(edge.to).addEdge(edge.from, edge.number, edge.transition);
            }
        }
        return transpose;
    }

    /**
     * Returns a new graph where every set of vertices in components is contracted to a single vertex.
     * TODO union-find
     */
    public Condenser contract(Components components) {
        Condenser contraction = new Condenser();

        for (Set<Vertex> component : components.components.values()) {
            Vertex root = getVert(components.roots.get(component.iterator().next().id));
            Vertex newRoot = new Vertex(root.id, new HashSet<>(), new Condenser());
            contraction.addVert(newRoot);
            for (Vertex vertex : component) {
                Vertex newVert = new Vertex(root.id + "." + vertex.id, new HashSet<>(), new Condenser());
                for (Edge edge : vertex.edges) {
                    if (components.roots.get(getVert(edge.to).id).equals(root.id)) {
                        // Edge inside the component
                        newVert.addEdge(root.id + "." + edge.to, edge.number, edge.transition);
                    }
                    else {
                        // Edge goes to vertex outside component
                        newRoot.addEdge(components.roots.get(edge.to), edge.number, edge.transition);
                    }
                }
                newRoot.contraction.addVert(newVert);
            }
        }

        return contraction;
    }

    /**
     * Removes all top-level vertices and nodes, brings up their contracted graphs.
     */
    public Condenser stepDown() {
        Condenser ret = new Condenser();
        for (Vertex vertex : vertices.values()) {
            ret.vertices.putAll(vertex.contraction.vertices);
        }
        return ret;
    }

    /**
     * Removes all contractions from vertices -- keeps only the top level, and adds back in any edges numbered less than mid.
     */
    public Condenser stepUp(int mid, Condenser g, Map<String, String> roots) {
        Condenser ret = new Condenser();
        for (Vertex vertex : vertices.values()) {
            vertex.contraction.vertices.clear();
            ret.addVert(vertex);
        }

        // TODO clean this up
        for (Vertex vertex : g.vertices.values()) {
            for (Edge edge : vertex.edges) {
                if (edge.number < mid) {
                    String from = roots.getOrDefault(edge.from, edge.from);
                    String to = roots.getOrDefault(edge.to, edge.to);
                    if (!to.equals(from)) {
                        ret.vertices.getOrDefault(from, new Vertex(from)).addEdge(to, edge.number, edge.transition);
                        if (!ret.vertices.containsKey(to)) {
                            ret.vertices.put(to, new Vertex(to));
                        }
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Returns a new condenser keeping only pedges passed by filter.
     */
    public Condenser filterEdges(EdgeFilter filter) {
        Condenser res = new Condenser();
        Set<String> vertsToKeep = new HashSet<>();

        for (Map.Entry<String, Vertex> vertEntry : vertices.entrySet()) {
            Vertex vertex = vertEntry.getValue();
            String id = vertEntry.getKey();
            Vertex newVert = new Vertex(id, new HashSet<>(), vertex.contraction.filterEdges(filter));
            for (Edge edge : vertex.edges) {
                // Filter out edges
                if (filter.filter(edge)) {
                    newVert.addEdge(edge.to, edge.number, edge.transition);
                    // We also want to keep any vertices with incoming edges
                    vertsToKeep.add(edge.to);
                }
            }
            res.addVert(newVert);

            // We want to keep this vertex if it has outgoing edges
            if (!newVert.edges.isEmpty()) {
                vertsToKeep.add(id);
            }
        }

        // Only keep vertices that are connected (those in vertsToKeep)
        res.vertices.keySet().retainAll(vertsToKeep);

        return res;
    }

    public String toString() {
        return vertices.toString();
    }

    public boolean isEmpty() {
        return vertices.size() == 0;
    }

    /**
     * Returns the maximum number of an edge in the graph.
     * Does not recurse into contractions
     */
    public int getMaxNum() {
        Optional<Integer> max = vertices.values().stream().flatMap(v -> v.edges.stream().map(e -> e.number))
            .max(Comparator.naturalOrder());
        if (!max.isPresent()) {
            return 0;
        }
        return max.get();
    }
}
