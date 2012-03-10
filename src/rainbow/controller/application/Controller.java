package rainbow.controller.application;

import rainbow.controller.events.Event;
import rainbow.controller.node.Node;
import rainbow.controller.factory.ControllerFactory;
import rainbowpc.controller.*;
import rainbowpc.Message;
import rainbowpc.RainbowException;
import rainbowpc.controller.messages.*;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.TreeMap;
import java.util.Timer;
import java.util.PriorityQueue;

public class Controller {
	////////////////////////////////////////////////////////////////////////////
	// Attributes
	//
	private static final int NODE_REGISTER_TIMEOUT = 5000; //5 sec
	private static final int NODE_PREALLOCATE_CAPACITY = 50;

	private ControllerProtocol protocol;

	private String id = "uninitialized";
	private int stringLength = 0;
	private String target;
	private String algorithm;
	private PriorityQueue<Node> nodes = new PriorityQueue<Node>(NODE_PREALLOCATE_CAPACITY);

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

	public PriorityQueue<Node> getNodes() {
		return nodes;
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

	////////////////////////////////////////////////////////////////////////////
	// Node priority queue management
	//
	public void addNode(Node node) {
		nodes.offer(node);
	}	

	////////////////////////////////////////////////////////////////////////////
	// Helper methods
	//
	public void log(String msg) {
		// System.out for now
		System.out.println(msg);
	}

	public void warn(String msg) {
		System.out.println("[* WARN *] " + msg);
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
