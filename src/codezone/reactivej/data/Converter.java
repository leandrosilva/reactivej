package codezone.reactivej.data;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Converter {

	public ByteBuffer toByteBuffer(String content) {
		return toByteBuffer(content.getBytes());
	}

	public ByteBuffer toByteBuffer(byte[] content) {
		return ByteBuffer.wrap(content);
	}
	
	public String toString(byte[] content) {
		return toString(ByteBuffer.wrap(content));
	}
	
	public String toString(ByteBuffer content) {
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();

		try {
			return decoder.decode(content).toString();
		} catch (CharacterCodingException e) {
			throw new RuntimeException("Error while decoding the ByteBuffer content", e);
		}
	}
	
	public byte[] toBytes(ByteBuffer content) {
		return content.array();
	}
}
