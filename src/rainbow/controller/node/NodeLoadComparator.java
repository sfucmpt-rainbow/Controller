package rainbow.controller.node;

import java.util.Comparator;

public class NodeLoadComparator implements Comparator<Node> {
	public int compare(Node node0, Node node1) {
		// inverted because lower load should get higher priority
		return Float.compare(node1.getLoad(), node0.getLoad());
	}
}
