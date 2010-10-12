package codezone.reactivej;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;



public class ServerImpl implements Server {

	private boolean running = false;

	private InitiationDispatcher initiationDispatcher;

	private InetAddress address;
	private int port;
	
	public ServerImpl(int port, Class<? extends EventHandler> eventHandlerClass) {
		this(port,
				InitiationDispatcherImpl.class,
				HandleImpl.class,
				eventHandlerClass);
	}
	
	public ServerImpl(
			int port,
			Class<? extends InitiationDispatcher> initiationDispatcherClass,
			Class<? extends Handle> handleClass,
			Class<? extends EventHandler> eventHandlerClass) {
		
		this.port = port;
		this.address = getLocalHostAddress();
		
		initiationDispatcher = newInitiationDispatcher(handleClass, initiationDispatcherClass);
		initiationDispatcher.registerHandler(eventHandlerClass);
	}
	
	private InetAddress getLocalHostAddress() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException("Error getting the localhost address", e);
		}
	}
	
	private InitiationDispatcher newInitiationDispatcher(
			Class<? extends Handle> handleClass,
			Class<? extends InitiationDispatcher> initiationDispatcherClass) {
		
		try {
			return initiationDispatcherClass.getConstructor(
						Server.class,
						Class.class).newInstance(this, handleClass);
		} catch (Exception e) {
			throw new RuntimeException("Error while instantiating the initiation dispatcher class", e);
		} 
	}

	public InetAddress address() {
		return address;
	}

	public int port() {
		return port;
	}
	
	public boolean isRunning() {
		return running;
	}

	public void run() {
        if (isRunning()) {
            throw new IllegalStateException("Server already is running.");
        }
        
        running = true;
        
		new Thread(
			new Runnable() {
				public void run() {
					try {
						initiationDispatcher.handleEvents();
					} catch (IOException e) {
						throw new RuntimeException("Error while handling events", e);
					}		
				}
			}
		).start();
	}

	public boolean isStopped() {
		return !running;
	}
	
	public void stop() {
        if (isStopped()) {
            throw new IllegalStateException("Server already is stoped.");
        }
        
		running = false;
	}
}
