package rainbow.controller.events;

import rainbowpc.Message;

public abstract class Event {
	public void run(Message msg) {
		action(msg);
	}

	public abstract void action(Message msg);
}
