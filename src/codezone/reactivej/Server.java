package codezone.reactivej;

import java.net.InetAddress;

public interface Server {
	
	InetAddress address();
	int port();
	
	boolean isRunning();
	void run();
	
	boolean isStopped();
	void stop();
}
