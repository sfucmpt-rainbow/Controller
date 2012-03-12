package rainbow.controller.node;

import rainbow.controller.workBlock.WorkBlock;
import rainbowpc.controller.ControllerProtocolet;
import rainbowpc.controller.messages.NewNodeMessage;
import rainbowpc.node.messages.*;
import java.util.ArrayList;
import java.io.IOException;

public class Node implements Comparable<Node> {
	private static final int NO_THREADS = 0;

	private String name;
	private int threads;
	private boolean alive; // nodes may die ay any given time, we must 
							// prevent assigning further work
	private ControllerProtocolet agent;
	private ArrayList<WorkBlock> assigned;
	
	public Node(NewNodeMessage msg) {
		this(msg.getName(), msg.getCoreCount(), msg.getAgent());
	}

	public Node(String name, int cores, ControllerProtocolet agent) {
		this.name = name;
		threads = cores;     
		this.agent = agent;
		alive = true;
		assigned = new ArrayList<WorkBlock>();
	}

	public int compareTo(Node node) {
		//return Float.compare(getLoad(), node.getLoad());
		return name.compareTo(node.name);
	}

	public boolean equals(Object o) {
		return getName() == o;
	}

	public int setThreads(int n) {
		if (n > NO_THREADS) {
			threads = n;
		}
		return threads;
	}

	public float getLoad() {
		return assigned.size()/threads;
	}

	public String getName() {
		return name;
	}

	public int getThreadCount() {
		return threads;
	}

	public boolean isAlive() {
		return alive;
	}

	public void kill() {
		alive = false;
	}

	public void clearAllJobs() {
		assigned.clear();
	}

	public void removeBlock(WorkBlock block) {
		assigned.remove(block);
	}

	public float assignBlock(WorkBlock block, String target) {
		try {
			agent.sendMessage(new WorkMessage(
				name,
				target,
				block.getPartitionId(), 
				block.getId(), 
				block.getStartBlockNumber(), 
				block.getEndBlockNumber(),
				block.getStringLength()
			));
		} catch (IOException e) {
			alive = false;
		}
		assigned.add(block);
		return getLoad();
	}

	public void requeueAllAssignedWork() {
		for (WorkBlock block : assigned) {
			block.getPartition().repushBlock(block.getId());
		}
	}

}
