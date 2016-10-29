package pl.karol202.weather.hardware;

import java.io.IOException;
import java.io.InputStream;

public class DataUtils
{
	public static int bytesToInt(InputStream stream) throws IOException
	{
		return (stream.read()        & 0xff) |
			  ((stream.read() << 8 ) & 0xff00) |
			  ((stream.read() << 16) & 0xff0000) |
			  ((stream.read() << 24) & 0xff000000);
	}
	
	public static byte[] intToBytes(int number)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte)  (number        & 0xff);
		bytes[1] = (byte) ((number >> 8 ) & 0xff);
		bytes[2] = (byte) ((number >> 16) & 0xff);
		bytes[3] = (byte) ((number >> 24) & 0xff);
		return bytes;
	}
}
