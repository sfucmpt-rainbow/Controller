package prainbow;

import rainbowpc.controller.*;
import rainbowpc.Message;
import rainbowpc.RainbowException;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Controller {
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
		Executor protocolExecutor = Executors.newSingleThreadExecutor();
		protocolExecutor.execute(protocol);
		protocol.setInterruptThread(Thread.currentThread());
		while (true) {
			try {
				protocol.getMessage();
			}
			catch (InterruptedException e) {
				break;
			}
		}	
		System.exit(0);
	}
}
