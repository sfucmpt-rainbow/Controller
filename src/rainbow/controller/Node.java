package rainbow.controller.nodes;

public class Node implements Comparable<Node> {
	float load = 0.0f;

	public int compareTo(Node node) {
		return Float.compare(load, node.load);
	}
}
