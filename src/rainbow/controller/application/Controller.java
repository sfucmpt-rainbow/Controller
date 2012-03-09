package rainbow.controller.application;

import rainbow.controller.events.Event;
import rainbow.controller.node.Node;
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
	private static final int NODE_REGISTER_TIMEOUT = 5000; //10 sec

	private static ControllerProtocol protocol;

	private static String id = "uninitialized";
	private static int stringLength = 0;
	private static String target;
	private static String algorithm;
	private static PriorityQueue<Node> nodes = new PriorityQueue<Node>();

	static TreeMap<String, Event> eventMapping = new TreeMap<String, Event>();
	static {
		eventMapping.put(ControllerBootstrapMessage.LABEL, new Event() {
			public void action(Message msg) {
				ControllerBootstrapMessage bootstrap = (ControllerBootstrapMessage)msg;
				id = bootstrap.id;
				log("Set id to " + id);
			}
		});

		eventMapping.put(NewQuery.LABEL, new Event() {
			public void action(Message msg) {
				NewQuery query = (NewQuery)msg;
				target = query.getQuery();
				algorithm = query.getHashMethod();
			}
		});
				
		
		eventMapping.put(WorkBlockSetup.LABEL, new Event() {
			public void action(Message msg) {
				WorkBlockSetup setup = (WorkBlockSetup)msg;
				stringLength = setup.getStringLength();
				log("Length set to " + id);
			}
		});

		eventMapping.put(NewNodeMessage.LABEL, new Event() {
			public void action(Message msg) {
				NewNodeMessage nodeMsg = (NewNodeMessage)msg;
				Node node = new Node(nodeMsg);
				nodes.offer(node);
				log(node.getName() + " has joined the collective!");
			}
		});

	}

	private static void log(String msg) {
		// System.out for now
		System.out.println(msg);
	}

	private static void warn(String msg) {
		System.out.println("[* WARN *] " + msg);
	}

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
		protocol = connectToScheduler(args[0]);
		if (protocol == null) {
			System.err.println("Failed to connect o scheduler " + args[0]);
			System.exit(1);
		}
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
		System.exit(0);
	}
}
