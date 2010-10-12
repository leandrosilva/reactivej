package codezone.reactivej.protocol;

public class HttpProtocol implements Protocol {
	
	private String serverName;
	
	public HttpProtocol(String serverName) {
		this.serverName = serverName;
	}

	public String decodeRequest(String content) {
		// TODO implementar HttpProtocol.decodeRequest
		throw new UnsupportedOperationException("Need to be implemented");
	}

	public String encodeResponse(int status, String message, String contentType, String body) {
		// TODO ainda esta bem fake, precisa fazer uma implementacao de verdade
		return String.format(
					"HTTP/1.1 %d %s\r\n" +
					"Etag: \"b32ffe9242ccde98d6e19ed4c9dbc053d4a18155\"\r\n" +
					"Content-Length: %d\r\n" +
					"Content-Type: %s\r\n" +
					"Server: %s\r\n\r\n" +
					"%s\r\n",
					status, message, body.length(), contentType, serverName, body);
	}
}
