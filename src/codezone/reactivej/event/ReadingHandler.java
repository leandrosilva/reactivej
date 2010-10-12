package codezone.reactivej.event;

import java.io.IOException;

import codezone.reactivej.EventHandler;

public interface ReadingHandler extends EventHandler {

	void onRead() throws IOException;
}
