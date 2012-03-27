/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rainbow.controller.application;

import java.util.Currency;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import rainbowpc.Message;
import rainbowpc.controller.messages.ControllerBootstrapMessage;
import rainbowpc.controller.messages.NewQuery;
import rainbowpc.controller.messages.StopQuery;
import rainbowpc.controller.messages.WorkBlockSetup;

/**
 *
 * @author WesleyLuk
 */
public class ControllerMappingFactory {

	public static HashMap<String, Action> createMapping(final Controller controller) {
		HashMap<String, Action> mapping = new HashMap<String, Action>();
		mapping.put(NewQuery.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				controller.query = (NewQuery) message;
				Logger.getAnonymousLogger().log(Level.INFO, "New Query recieved");
			}
		});
		mapping.put(StopQuery.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				controller.current.interrupt();
				controller.query = null;
				Logger.getAnonymousLogger().log(Level.INFO, "Stop Query recieved");
			}
		});
		mapping.put(ControllerBootstrapMessage.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				Logger.getAnonymousLogger().log(Level.INFO, "ControllerBootstrapMessage recieved");
			}
		});
		mapping.put(WorkBlockSetup.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				WorkBlockSetup workBlock = (WorkBlockSetup) message;
				System.out.println(String.format("WorkBlockSetup recieved (%s,%s,%s)",
						workBlock.getStartBlockNumber(),
						workBlock.getEndBlockNumber(),
						workBlock.getStringLength()));
				controller.bruteForce(workBlock);
			}
		});
		return mapping;
	}
}
