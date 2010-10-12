package samples.codezone.reactivej.tcp.echoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import codezone.reactivej.data.Converter;

public class EchoClient {
	
	private class MessageReceiver extends Thread {
		private SocketChannel clientSocketChannel;
		private boolean shouldReceive;

		public MessageReceiver(String name, SocketChannel clientSocketChannel) {
			super(name);
			
			this.clientSocketChannel = clientSocketChannel;
		}
		
		public void startReceiving() {
			shouldReceive = true;
			start();
		}
		
		public void stopReceiving() {
			shouldReceive = false;
		}

		public void run() {
			System.out.println("Inside receive message");
			
			ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
			
			try {
				while (shouldReceive) {
					while (clientSocketChannel.read(byteBuffer) > 0) {
						byteBuffer.flip();
						
						System.out.println(new Converter().toString(byteBuffer));
						
						byteBuffer.flip();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	///
	
	private SocketChannel clientSocketChannel;
	private MessageReceiver messageReceiver;

	public void makeConnection() {
		try {
			clientSocketChannel = setupClientSocketChannel();
		} catch (IOException e) {
			e.printStackTrace();
		}

		startReceivingMessage();
		
		while (sendMessage() != -1) {}
		
		stopReceivingMessage();
		
		try {
			Thread.sleep(2000);

			clientSocketChannel.close();
			
			System.out.println("Stopped!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private SocketChannel setupClientSocketChannel() throws IOException {
		InetSocketAddress serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 8888);
		
		clientSocketChannel = SocketChannel.open();
		clientSocketChannel.connect(serverAddress);
		clientSocketChannel.configureBlocking(false);
		
		return clientSocketChannel;
	}

	private void startReceivingMessage() {
		messageReceiver = new MessageReceiver("MessageReceiver Thread", clientSocketChannel);
		messageReceiver.startReceiving();
	}

	private void stopReceivingMessage() {
		messageReceiver.stopReceiving();
	}

	private int sendMessage() {
		System.out.println("Inside SendMessage");
		
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String message = null;
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		int numberOfReadBytes = 0;
		
		try {
			System.out.print(">> ");
			message = input.readLine();

			if (message.equals("quit") || message.equals("shutdown")) {
				System.out.println("Time to stop the client");
				
				return -1;
			}
			
			byteBuffer = ByteBuffer.wrap(message.getBytes());
			numberOfReadBytes = clientSocketChannel.write(byteBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return numberOfReadBytes;
	}

	public static void main(String args[]) {
		EchoClient client = new EchoClient();
		client.makeConnection();
	}
}
