package codezone.reactivej;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HandleImpl implements Handle {

	private static final int MAX_CAPACITY_OF_BUFFER = 1024;
	private static final byte[] NO_CONTENT = new byte[0];

	private static final String DELIMITER = "\r\n";
	private static final byte[] DELIMITER_AS_BYTES = DELIMITER.getBytes();

	private SocketChannel socketChannel;
	private SelectionKey selectionKey;
	
	private Queue<ByteBuffer> bufferQueue;
	private boolean shouldUseBuffer = false;
	
	public HandleImpl(SocketChannel socketChannel) throws IOException {
		
		this.socketChannel = socketChannel;
		
		adjustNonblockingMode();
	}

	private void adjustNonblockingMode() throws IOException {
		if (socketChannel.isBlocking()) {
			socketChannel.configureBlocking(false);
		}
	}
	
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
	
	public SelectionKey register(Selector synchronousEventDemultiplexer, int events)
		throws ClosedChannelException {
		
		selectionKey = socketChannel.register(synchronousEventDemultiplexer, events);
		
		return selectionKey;
	}

	public SelectionKey registerToRead(Selector synchronousEventDemultiplexer)
		throws ClosedChannelException {
		
		return register(synchronousEventDemultiplexer, SelectionKey.OP_READ);
	}

	public SelectionKey registerToWrite(Selector synchronousEventDemultiplexer)
		throws ClosedChannelException {
		
		return register(synchronousEventDemultiplexer, SelectionKey.OP_WRITE);
	}

	public SelectionKey registerToReadAndWrite(Selector synchronousEventDemultiplexer)
		throws ClosedChannelException {
		
		return register(
					synchronousEventDemultiplexer,
					SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}
	
	public boolean isRegisteredToRead() {
		return selectionKey != null && selectionKey.interestOps() == SelectionKey.OP_READ;
	}

	public boolean isRegisteredToWrite() {
		return selectionKey != null && selectionKey.interestOps() == SelectionKey.OP_WRITE;
	}

	public boolean isRegisteredToReadAndWrite() {
		return selectionKey != null
				&& selectionKey.interestOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	public SelectionKey getRegistration() {
		return selectionKey;
	}

	public void unregister() throws IOException {
		if (selectionKey != null) {
			selectionKey.cancel();
		}
	}
	
	public void finish() throws IOException {
		if (socketChannel.isOpen()) {
			socketChannel.close();
		}
		
		unregister();
	}

	public byte[] read() throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_CAPACITY_OF_BUFFER);

        // TODO nao seria melhor ter um while aqui? Porque pode ser que nao leia "tudo de uma vez"
		int numberOfReadBytes = socketChannel.read(byteBuffer);
		
		if (numberOfReadBytes == -1) {
			finish();
			
			return NO_CONTENT;
		} else {
			byteBuffer.flip();
	
			return byteBuffer.array();
		}
	}

	public int write(byte[] content) throws IOException {
        // TODO nao seria melhor ter um while aqui? Porque pode ser que nao escreve "tudo de uma vez"
		if (isBuffered()) {
			return writeOnQueue(content);
		} else {
			return writeOnSocket(content);
		}
	}

	private int writeOnQueue(byte[] content) throws IOException {
		ByteBuffer byteBuffer = wrapIntoByteBuffer(content);
		
		bufferQueue.add(byteBuffer);
		
		return byteBuffer.position();
	}

	private int writeOnSocket(byte[] content) throws IOException {
		ByteBuffer byteBuffer = wrapIntoByteBuffer(content);
		
		return socketChannel.write(byteBuffer);
	}
	
	private ByteBuffer wrapIntoByteBuffer(byte[] content) {
		byte[] fullContent = contentWithDelimiter(content);
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_CAPACITY_OF_BUFFER);
		byteBuffer = ByteBuffer.wrap(fullContent);
		
		return byteBuffer;
	}

	private byte[] contentWithDelimiter(byte[] content) {
		byte[] fullContent = new byte[content.length + DELIMITER_AS_BYTES.length];
		
		System.arraycopy(
				content, 0, fullContent,
				0, content.length);
		
		System.arraycopy(
				DELIMITER_AS_BYTES, 0,
				fullContent, content.length, DELIMITER_AS_BYTES.length);
		
		return fullContent;
	}

	public void useBuffer(boolean value) {
		shouldUseBuffer = value;
		bufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	}

	public boolean isBuffered() {
		return shouldUseBuffer;
	}

	public void flushBuffer() throws IOException {
		ByteBuffer bufferedContent;
		byte[] content;
		
		while ((bufferedContent = bufferQueue.poll()) != null) {
			content = new byte[bufferedContent.capacity()];
			bufferedContent.get(content);

			writeOnSocket(content);
		}
	}
}
