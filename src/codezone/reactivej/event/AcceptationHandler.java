package codezone.reactivej.event;

import java.io.IOException;

import codezone.reactivej.EventHandler;

public interface AcceptationHandler extends EventHandler {

	void onAccept() throws IOException;
}
