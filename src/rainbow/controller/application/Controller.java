package rainbow.controller.application;

import rainbow.controller.events.Event;
import rainbowpc.controller.*;
import rainbowpc.Message;
import rainbowpc.RainbowException;
import rainbowpc.controller.messages.*;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.TreeMap;

public class Controller {
	static ControllerProtocol protocol;

	static String id = "uninitialized";
	static int stringLength = 0;
	static String target;
	static String algorithm;

	static TreeMap<String, Event> eventMapping = new TreeMap<String, Event>();
	static {
		eventMapping.put(ControllerBootstrapMessage.LABEL, new Event() {
			public void action(Message msg) {
				ControllerBootstrapMessage bootstrap = (ControllerBootstrapMessage)msg;
				id = bootstrap.id;
				System.out.println("Set id to " + id);
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
				System.out.println("Length set to " + id);
			}
		});
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
					System.out.println("Message with method " + msg.getMethod() + " dropped");
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
