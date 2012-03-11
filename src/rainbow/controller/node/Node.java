package rainbow.controller.node;

import rainbowpc.controller.messages.NewNodeMessage;

public class Node implements Comparable<Node> {
	private static final int NO_THREADS = 0;

	private String name;
	private int threads;
	private int jobs;
	private boolean alive; // nodes may die ay any given time, we must 
							// prevent assigning further work
	
	public Node(NewNodeMessage msg) {
		this(msg.getName(), msg.getCoreCount());
	}

	public Node(String name, int cores) {
		this.name = name;
		threads = cores;     // must have at least one, or else, wtf?
		jobs = 0;
		alive = true;
	}

	public int compareTo(Node node) {
		//return Float.compare(getLoad(), node.getLoad());
		return name.compareTo(node.name);
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

	public int getThreadCount() {
		return threads;
	}
}
