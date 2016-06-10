/**
 * Witness for non-emptiness.
 * Contains an edge and the corresponding SCC.
 */
public class Witness
{
    public Edge edge;
    public Condenser component;

    public Witness(Edge edge, Condenser component) {
        this.edge = edge;
        this.component = component;
    }
}
