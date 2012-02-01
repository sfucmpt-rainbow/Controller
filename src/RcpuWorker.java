import com.ericsson.otp.erlang.*;

// A core can be thought of as a node
class RcpuWorker extends Thread {
	private int id;				   			// id
	private OtpErlangPid rsg_pid;           // The rsg worker allocated for core

	RcpuWorker(int id, OtpErlangPid pid) {
		this.id = id;
		this.rsg_pid = pid;
	}

	public void run() {
	}

}
