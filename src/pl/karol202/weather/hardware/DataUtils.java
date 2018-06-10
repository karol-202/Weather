package pl.karol202.weather.hardware;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DataUtils
{
	public static int bytesToInt(byte[] bytes)
	{
		return (bytes[0]        & 0xff)     |
			  ((bytes[1] << 8 ) & 0xff00)   |
			  ((bytes[2] << 16) & 0xff0000) |
			  ((bytes[3] << 24) & 0xff000000);
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
	
	public static float bytesToFloat(byte[] bytes)
	{
		return ByteBuffer.wrap(bytes).getFloat();
	}
	
	public static byte[] floatToBytes(float number)
	{
		return ByteBuffer.allocate(4).putFloat(number).array();
	}
}
