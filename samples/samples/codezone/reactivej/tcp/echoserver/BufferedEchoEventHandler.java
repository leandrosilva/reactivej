package samples.codezone.reactivej.tcp.echoserver;

import java.io.IOException;

import codezone.reactivej.Handle;
import codezone.reactivej.event.AcceptationHandler;
import codezone.reactivej.event.WritingHandler;


public class BufferedEchoEventHandler extends EchoEventHandler
	implements AcceptationHandler, WritingHandler {
	
	public BufferedEchoEventHandler(Handle handle) {
		super(handle);
	}

	public void onAccept() throws IOException {
		getHandle().useBuffer(true);
	}

	public void onWrite() throws IOException {
		getHandle().flushBuffer();
	}
}
