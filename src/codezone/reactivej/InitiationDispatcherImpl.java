package codezone.reactivej;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import codezone.reactivej.event.AcceptationHandler;
import codezone.reactivej.event.ReadingHandler;
import codezone.reactivej.event.WritingHandler;


public class InitiationDispatcherImpl implements InitiationDispatcher {

	private Server server;
	
	private Selector synchronousEventDemultiplexer;
	private ServerSocketChannel serverSocketChannel;
	private Class<? extends Handle> handleClass;
	private Class<? extends EventHandler> eventHandlerClass;

	public InitiationDispatcherImpl(Server server, Class<? extends Handle> handleClass) {
		this.server = server;
		this.handleClass = handleClass;
	}

	public void registerHandler(Class<? extends EventHandler> eventHandlerClass) {
		this.eventHandlerClass = eventHandlerClass;
	}

	public void handleEvents() throws IOException {
		synchronousEventDemultiplexer = Selector.open();

		try {
			serverSocketChannel = createServerSocketChannel();
			serverSocketChannel.register(synchronousEventDemultiplexer, SelectionKey.OP_ACCEPT);
			
			eventLoop();
		} finally {
			serverSocketChannel.close();
		}
	}
	
	private void eventLoop() throws IOException {
		Iterator<SelectionKey> selectedKeys;
		SelectionKey selectedKey;
		
		boolean hasAcceptationHandler = hasAcceptationHandler();
		boolean hasReadingHandler = hasReadingHandler();
		boolean hasWritingHandler = hasWritingHandler();
		
		while (server.isRunning()) {
			// TODO seria interessante botar um timeout nesse select
			if (synchronousEventDemultiplexer.select() > 0 && server.isRunning()) {
				selectedKeys = synchronousEventDemultiplexer.selectedKeys().iterator();
				
				while (selectedKeys.hasNext()) {
					selectedKey = selectedKeys.next();
					selectedKeys.remove();
					
					if (isAcceptable(selectedKey)) {
						Handle handle = acceptNewSocketChannel();
						SelectionKey selectionKey = handle.getRegistration();
						
						attachEventHandler(newEventHandler(handle), selectionKey);

						if (hasAcceptationHandler) {
							AcceptationHandler eventHandler = getEventHandler(selectionKey);

							try {
								eventHandler.onAccept();
							} catch (Exception e) {
								eventHandler.onError("onAccept", e);
							}
						}
					}
					
					if (hasReadingHandler && isReadable(selectedKey)) {
						ReadingHandler eventHandler = getEventHandler(selectedKey);
						
						try {
							eventHandler.onRead();
						} catch (Exception e) {
							eventHandler.onError("onRead", e);
						}
					}
					
					if (hasWritingHandler && isWritable(selectedKey)) {
						WritingHandler eventHandler = getEventHandler(selectedKey);

						try {
							eventHandler.onWrite();
						} catch (Exception e) {
							eventHandler.onError("onWrite", e);
						}
					}
				}
			}
		}
	}

	private ServerSocketChannel createServerSocketChannel() throws IOException {
		try {
			InetSocketAddress serverAddress = new InetSocketAddress(server.address(), server.port());
			
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(serverAddress);
			
			return serverSocketChannel;
		} catch (UnknownHostException e) {
			throw new RuntimeException("Error while creating the server socket channel", e);
		}
	}

	private Handle acceptNewSocketChannel() throws IOException {
		SocketChannel socketChannel = (SocketChannel) serverSocketChannel.accept();
		Handle handle = newHandle(socketChannel);
		handle.register(synchronousEventDemultiplexer, getHandlerInterest());
		
		return handle;
	}
	
	private Handle newHandle(SocketChannel socketChannel) {

		try {
			return handleClass.getConstructor(SocketChannel.class).newInstance(socketChannel);
		} catch (Exception e) {
			throw new RuntimeException("Error while instantiating the handle class", e);
		} 
	}
	
	private int getHandlerInterest() {
		int interestEvents = 0;
		
		if (hasReadingHandler()) {
			interestEvents = SelectionKey.OP_READ;
		}
		
		if (hasWritingHandler()) {
			interestEvents = interestEvents | SelectionKey.OP_WRITE;
		}
		
		return interestEvents;
	}

	private EventHandler newEventHandler(Handle handle) {
		try {
			return (EventHandler) eventHandlerClass.getConstructor(Handle.class).newInstance(handle);
		} catch (Exception e) {
			throw new RuntimeException("Error while instantiating the event handler class", e);
		} 
	}

	private void attachEventHandler(EventHandler eventHandler, SelectionKey selectionKey) {
		selectionKey.attach(eventHandler);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getEventHandler(SelectionKey selectedKey) {
		return (T) selectedKey.attachment();
	}
	
	private boolean hasAcceptationHandler() {
		return AcceptationHandler.class.isAssignableFrom(eventHandlerClass);
	}
	
	private boolean isAcceptable(SelectionKey selectedKey) {
		return selectedKey.isValid() && selectedKey.isAcceptable();
	}
	
	private boolean hasReadingHandler() {
		return ReadingHandler.class.isAssignableFrom(eventHandlerClass);
	}
	
	private boolean isReadable(SelectionKey selectedKey) {
		return selectedKey.isValid() && selectedKey.isReadable();
	}
	
	private boolean hasWritingHandler() {
		return WritingHandler.class.isAssignableFrom(eventHandlerClass);
	}
	
	private boolean isWritable(SelectionKey selectedKey) {
		return selectedKey.isValid() && selectedKey.isWritable();
	}
}
