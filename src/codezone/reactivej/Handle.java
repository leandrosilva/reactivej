package codezone.reactivej;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public interface Handle {
	
	SocketChannel getSocketChannel();

	SelectionKey register(Selector synchronousEventDemultiplexer, int events)
		throws ClosedChannelException;
	
	SelectionKey registerToRead(Selector synchronousEventDemultiplexer)
		throws ClosedChannelException;
	SelectionKey registerToWrite(Selector synchronousEventDemultiplexer)
		throws ClosedChannelException;
	SelectionKey registerToReadAndWrite(Selector synchronousEventDemultiplexer)
		throws ClosedChannelException;
	
	boolean isRegisteredToRead();
	boolean isRegisteredToWrite();
	boolean isRegisteredToReadAndWrite();
	
	SelectionKey getRegistration();
	
	void unregister() throws IOException;
	
	void finish() throws IOException;
	
	byte[] read() throws IOException;
	int write(byte[] content) throws IOException;
	
	void useBuffer(boolean value);
	boolean isBuffered();
	void flushBuffer() throws IOException;
}
