package rainbow.controller.factory;

import rainbow.controller.application.Controller;
import rainbow.controller.events.Event;
import rainbow.controller.node.Node;
import rainbow.controller.workBlock.WorkBlockPartition;
import rainbowpc.controller.messages.*;
import rainbowpc.node.messages.*;
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
			}
		});
				
		
		eventMapping.put(WorkBlockSetup.LABEL, new Event() {
			public void action(Message msg) {
				WorkBlockSetup setup = (WorkBlockSetup)msg;
				controller.setStringLength(setup.getStringLength());
				controller.log("Length set to " + controller.getStringLength());
				controller.setBlockLength(Controller.TEST_BLOCK_LENGTH);
				controller.setAlphabet(Controller.TEST_ALPHA);
				controller.assignWorkPartition(Controller.TEST_ID, setup.getStartBlockNumber(), setup.getEndBlockNumber(), setup.getStringLength());
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

		eventMapping.put(NodeDisconnectMessage.LABEL, new Event() {
			public void action(Message msg) {
				NodeDisconnectMessage info = (NodeDisconnectMessage)msg;
				controller.gracefulTerminate(info.getId());
				controller.log("Signalled that " + info.getId() + " has disconnected");
			}
		});

		eventMapping.put(WorkMessage.LABEL, new Event() {
			public void action(Message msg) {
				WorkMessage details = (WorkMessage)msg;
				if (details.targetFound()) {
					controller.markTargetFound(details.getPartitionId(), details.getReversed());
				} else {
					controller.markBlockDone(details.getNodeName(), details.getPartitionId(), details.getBlockId());
				}
				controller.distributeWork();
			}
		});

		return eventMapping;
	}
}
