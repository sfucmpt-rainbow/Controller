
package rainbow.controller.application;

import rainbowpc.controller.messages.WorkBlockSetup;

public interface BruteForceEventListener {

	public void matchFound(String match);

	public void workBlockComplete(WorkBlockSetup b);
}
