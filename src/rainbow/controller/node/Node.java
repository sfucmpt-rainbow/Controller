package rainbow.controller.node;

import rainbowpc.controller.messages.NewNodeMessage;

public class Node implements Comparable<Node> {
	private static final int NO_THREADS = 0;

	private String name;
	private int threads;
	private int jobs;
	
	public Node(NewNodeMessage msg) {
		this(msg.getName());
	}

	public Node(String name) {
		this.name = name;
		threads = 1;     // must have at least one, or else, wtf?
		jobs = 0;
	}

	public int compareTo(Node node) {
		return Float.compare(getLoad(), node.getLoad());
	}

	public int setThreads(int n) {
		if (n > NO_THREADS) {
			threads = n;
		}
		return threads;
	}

	public float getLoad() {
		return threads/jobs;
	}

	public String getName() {
		return name;
	}
}
