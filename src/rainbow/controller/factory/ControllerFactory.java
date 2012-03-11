package rainbow.controller.factory;

import rainbow.controller.application.Controller;
import rainbow.controller.events.Event;
import rainbow.controller.node.Node;
import rainbowpc.controller.messages.*;
import rainbowpc.Message;
import java.util.TreeMap;

public class ControllerFactory {
	public static TreeMap<String, Event> getDefaultMapping(final Controller controller) {
		TreeMap<String, Event> eventMapping = new TreeMap<String, Event>();

		eventMapping.put(ControllerBootstrapMessage.LABEL, new Event() {
			public void action(Message msg) {
				ControllerBootstrapMessage bootstrap = (ControllerBootstrapMessage)msg;
				controller.setId(bootstrap.id);
				controller.log("Set id to " + controller.getId());
			}
		});

		eventMapping.put(NewQuery.LABEL, new Event() {
			public void action(Message msg) {
				NewQuery query = (NewQuery)msg;
				controller.setTarget(query.getQuery());
				controller.log("Set target to " + controller.getTarget());
				controller.setAlgorithm(query.getHashMethod());
				controller.log("Set algorithm to " + controller.getAlgorithm());
				controller.distributeWork();
			}
		});
				
		
		eventMapping.put(WorkBlockSetup.LABEL, new Event() {
			public void action(Message msg) {
				WorkBlockSetup setup = (WorkBlockSetup)msg;
				controller.setStringLength(setup.getStringLength());
				controller.log("Length set to " + controller.getStringLength());
				controller.distributeWork();
			}
		});

		eventMapping.put(NewNodeMessage.LABEL, new Event() {
			public void action(Message msg) {
				NewNodeMessage nodeMsg = (NewNodeMessage)msg;
				Node node = new Node(nodeMsg);
				controller.addNode(node);
				controller.log(node.getName() + " has joined the collective!");
				controller.log(node.getName() + " has " + node.getThreadCount() + " threads");
				controller.distributeWork();
			}
		});

		return eventMapping;
	}
}
