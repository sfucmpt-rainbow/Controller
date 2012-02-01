import com.ericsson.otp.erlang.*;

class RcpuMaster extends Thread {
	RcpuMaster() {
		// trivial
	}

	public void run() {
		try {
			OtpNode node = new OtpNode("rcpu");
			OtpMbox mbox = node.createMbox();
			OtpErlangObject o;
			while (true) {
				try {
					o = mbox.receive();
					// we would spawn thread if this is a registration
					// request.
					// Otherwise, we would handle scheduler request.
				}
				catch (Exception e) {
					//
				}
			}
		}
		catch (java.io.IOException e) {
		}
		
	}
	
}
