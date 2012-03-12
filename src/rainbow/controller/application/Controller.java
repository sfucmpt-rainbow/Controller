package rainbow.controller.application;

import rainbow.controller.events.Event;
import rainbow.controller.node.Node;
import rainbow.controller.node.NodeLoadComparator;
import rainbow.controller.factory.ControllerFactory;
import rainbow.controller.workBlock.*;
import rainbowpc.controller.*;
import rainbowpc.Message;
import rainbowpc.RainbowException;
import rainbowpc.controller.messages.*;
import rainbowpc.scheduler.messages.WorkBlockComplete;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.TreeMap;
import java.util.Timer;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;

public class Controller {
	////////////////////////////////////////////////////////////////////////////
	// Attributes
	//
	private static final int NODE_REGISTER_TIMEOUT = 5000; //5 sec
	private static final int NODE_PREALLOCATE_CAPACITY = 50;
	// maximum load before a node is removed from the candidate queue
	private static final float NODE_LOAD_LIMIT = 10; 
	private static final int MAX_PARTITION_ID = 1023;

	public static final String TEST_ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	public static final int TEST_ID = 0;
	public static final int TEST_BLOCK_LENGTH = 100000000;  //100 mil

	private ControllerProtocol protocol;

	private String id = "uninitialized";
	private int stringLength = 0;
	private int nextPartitionId = 0;
	private int blockLength;
	private String target;
	private String algorithm;
	private String alphabet;
	private PriorityQueue<Node> candidates = 
		new PriorityQueue<Node>(NODE_PREALLOCATE_CAPACITY, new NodeLoadComparator());
	private TreeMap<String, Node> nodes = new TreeMap<String, Node>();
	private TreeMap<Integer, WorkBlockPartition> assignedPartitions = 
		new TreeMap<Integer, WorkBlockPartition>();

	////////////////////////////////////////////////////////////////////////////
	// Constructors
	//
	public Controller(ControllerProtocol protocol) {
		this.protocol = protocol;
	}

	////////////////////////////////////////////////////////////////////////////
	// Getters
	//	
	public String getId() {
		return id;
	}

	public int getStringLength() {
		return stringLength;
	}

	public String getTarget() {
		return target;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	////////////////////////////////////////////////////////////////////////////
	// Setters
	//

	public void setId(String id) {
		this.id = id;
	}

	public void setStringLength(int length) {
		stringLength = length;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}
	
	public void setBlockLength(int length) {
		this.blockLength = length;
	}

	////////////////////////////////////////////////////////////////////////////
	// Partition management methods
	//
	public void assignWorkPartition(int jobId, long startBlockNumber, long endBlockNumber, int stringLength) {
		WorkBlockPartition partition = new WorkBlockPartition(
			jobId, 
			getNextPartitionId(),
			startBlockNumber,
			endBlockNumber,
			alphabet, 
			target,
			stringLength,
			blockLength
		);
		assignedPartitions.put(partition.getPartitionId(), partition);
		log("New partition with " + partition.getSize() + " blocks assigned to controller!");
	}

	private int getNextPartitionId() {
		nextPartitionId = nextPartitionId < MAX_PARTITION_ID - 1? nextPartitionId++ : 0;
		return nextPartitionId;
	}

	////////////////////////////////////////////////////////////////////////////
	// Node priority queue management
	//
	public void addNode(Node node) {
		nodes.put(node.getName(), node);
		candidates.offer(node);
		log(node.getName() + " added to candidates");
	}	

	////////////////////////////////////////////////////////////////////////////
	// Helper methods
	//
	public void log(String msg) {
		// System.out for now
		System.out.println("[*** Controller ***]: " + msg);
	}

	public void warn(String msg) {
		System.out.println("[!!! Controller !!!]: " + msg);
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public void distributeWork() {
		log("Handing out work...");
		for (WorkBlockPartition partition : assignedPartitions.values()) {
			distributePartitionToNodes(partition);
			log(Integer.toString(partition.getCurrentSize()));
		}
		log("Distribution round complete");
	}

	private void distributePartitionToNodes(WorkBlockPartition partition) {
		while (!candidates.isEmpty() && partition.hasUnassignedWork()) {
			Node candidate = candidates.remove();
			float newLoad = candidate.assignBlock(partition.getNextBlock(), target);
			System.out.println(newLoad);
			if (newLoad < NODE_LOAD_LIMIT) 
				candidates.offer(candidate);
			log(candidate.getName() + " assigned work!");
		}
	}

	public void gracefulTerminate(String id) {
		Node node = nodes.remove(id);
		if (node != null) {
			node.kill();
			node.requeueAllAssignedWork();
			
		} else {
			warn(id + " not found in registered nodes!");
		}
	}

	public void markBlockDone(String nodeName, int partitionId, int blockId) {
		WorkBlockPartition partition = assignedPartitions.get(partitionId);
		if (partition != null) {
			WorkBlock block = partition.markBlockComplete(blockId);
			removeNodeWorkBlock(nodeName, block);
			if (partition.isDone()) {
				sendMessageToScheduler(new WorkBlockComplete(id, new WorkBlockSetup(
					partition.getStringLength(),
					partition.getStartBlockNumber(),
					partition.getEndBlockNumber()
				)));
			}
		}
		else {
			warn("Partition with id: " + partitionId + " not found");
		}
	}

	private void removeNodeWorkBlock(String nodeName, WorkBlock block) {
		Node node = nodes.get(nodeName);
		node.removeBlock(block);
		rescheduleNode(node);
	}

	private void rescheduleNode(Node node) {
			if (node.getLoad() < NODE_LOAD_LIMIT) {
			candidates.remove(node.getName());
			candidates.offer(node);
		}
	}

	public void markTargetFound(int partitionId, String reversed) {
		assignedPartitions.clear();
		for (Node node : nodes.values()) {
			node.clearAllJobs();
			rescheduleNode(node);
		}
		log("REVERSED!!!!! " + reversed);
	}

	public void sendMessageToScheduler(Message msg) {
		try {
			protocol.sendMessage(msg);
		} catch (IOException e) {
			protocol.shutdown();
			Thread.currentThread().interrupt();
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Main loop
	//
	public void run(TreeMap<String, Event> eventMapping) {
		Executor protocolExecutor = Executors.newSingleThreadExecutor();
		protocolExecutor.execute(protocol);
		protocol.setInterruptThread(Thread.currentThread());
		while (true) {
			try {
				Message msg = protocol.getMessage();
				Event event = eventMapping.get(msg.getMethod());
				if (event != null) {
					event.run(msg);
				}
				else {
					warn("Message with method " + msg.getMethod() + " dropped");
				}
			}
			catch (InterruptedException e) {
				break;
			}
		}	
		try {
			protocol.shutdown();
		}
		catch (Exception e) {}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Static main entry methods
	//
	private static boolean hasValidArguments(String[] args) {
		return args.length == 1;
	}

	private static ControllerProtocol connectToScheduler (String host) {
		try {
			return new ControllerProtocol(host);
		}
		// we don't care what exception is raised, just that we can't connect
		catch (Exception e) {
		}
		return null;
	}

	public static void main(String[] args) {
		if (!hasValidArguments(args)) {
			System.err.println("Controller takes the scheduler host as its only argument");
			System.exit(1);
		}
		ControllerProtocol protocol = connectToScheduler(args[0]);
		if (protocol == null) {
			System.err.println("Failed to connect o scheduler " + args[0]);
			System.exit(1);
		}
		Controller application = new Controller(protocol);
		application.run(ControllerFactory.getDefaultMapping(application));
		System.exit(0);
	}
}
