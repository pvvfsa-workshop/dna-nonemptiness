class Edge
{
    final String from;
    final String to;
    final int number;
    final Character transition;

    public Edge(String from, String to, int number, Character transition) {
        this.from = from;
        this.to = to;
        this.number = number;
        this.transition = transition;
    }

    public String toString() {
        return from + " --" + number + "-> " + to;
    }
}
