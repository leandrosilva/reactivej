package samples.codezone.reactivej.tcp.echoserver;

import java.io.IOException;

import codezone.reactivej.Handle;
import codezone.reactivej.event.ReadingHandler;
import codezone.reactivej.protocol.HttpProtocol;


public class HttpEchoEventHandler implements ReadingHandler {
	
	private Handle handle;
	private HttpProtocol protocol;
	
	public HttpEchoEventHandler(Handle handle) {
		this.handle = handle;
		this.protocol = new HttpProtocol("ReactiveJ/0.1");
	}

	public Handle getHandle() {
		return handle;
	}

	public void onRead() throws IOException {
//		byte[] content = handle.read();
//		
//		if (content.length > 0) {
//			handle.write(
//					protocol.encodeResponse(
//							200,
//							"OK",
//							"text/html; charset=UTF-8",
//							"Echoing: " + new String(content)).getBytes());
//		} else {
			handle.write(
					protocol.encodeResponse(
							200,
							"OK",
							"text/html; charset=UTF-8",
							"Hello World").getBytes());
//		}
		
		handle.finish();
	}

	public void onError(String event, Throwable exception)
		throws IOException {
		
		System.err.printf("[%s] An error occured: %s\n", event, exception.getMessage());
		
		exception.printStackTrace();
	}
}
