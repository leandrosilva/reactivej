package codezone.reactivej.event;

import java.io.IOException;

import codezone.reactivej.EventHandler;

public interface WritingHandler extends EventHandler {

	void onWrite() throws IOException;
}
