package codezone.reactivej;

import java.io.IOException;

public interface InitiationDispatcher {

	void registerHandler(Class<? extends EventHandler> eventHandlerClass);
	void handleEvents() throws IOException;
}
