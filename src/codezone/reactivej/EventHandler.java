package codezone.reactivej;

import java.io.IOException;


public interface EventHandler {
	
	Handle getHandle();

	void onError(String event, Throwable exception) throws IOException;
}
