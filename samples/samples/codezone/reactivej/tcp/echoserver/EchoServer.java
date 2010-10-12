package samples.codezone.reactivej.tcp.echoserver;

import codezone.reactivej.Server;
import codezone.reactivej.ServerImpl;


public class EchoServer {
	
	public static void main(String[] args) throws Exception {
		System.out.println("Running a TCP server for test");
		
		final Server fileServer = new ServerImpl(8888, HttpEchoEventHandler.class);
		fileServer.run();
	}
}
