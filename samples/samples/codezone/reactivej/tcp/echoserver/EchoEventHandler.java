package samples.codezone.reactivej.tcp.echoserver;

import java.io.IOException;

import codezone.reactivej.Handle;
import codezone.reactivej.data.Converter;
import codezone.reactivej.event.ReadingHandler;


public class EchoEventHandler implements ReadingHandler {
	
	private Handle handle;
	
	public EchoEventHandler(Handle handle) {
		this.handle = handle;
	}

	public Handle getHandle() {
		return handle;
	}

	public void onRead() throws IOException {
		byte[] content = handle.read();
		
		if (content.length > 0) {
			System.out.printf("[read] content: %s\n", new Converter().toString(content));

			handle.write(content);
		}
	}

	public void onError(String event, Throwable exception)
		throws IOException {
		
		System.err.printf("[%s] An error occured: %s\n", event, exception.getMessage());
		
		exception.printStackTrace();
	}
}
