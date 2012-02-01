import com.ericsson.otp.erlang.*;

public class Rcpu {
	
	public static void main(String[] argv) throws Exception {
		RcpuMaster master = new RcpuMaster();
		master.setPriority(Thread.MAX_PRIORITY);
		master.start();
		master.join();				// blocks for program life
	}
}
