package pl.karol202.weather;

import java.io.*;
import java.util.ArrayList;

public class RecordsManager
{
	private static ArrayList<Record> recordsMeasure;
	private static ArrayList<Record> recordsForecast;
	private static File file;
	
	public static void init()
	{
		recordsMeasure = new ArrayList<>();
		recordsForecast = new ArrayList<>();
		try
		{
			file = new File("data.dat");
			if(!file.exists()) file.createNewFile();
			else load();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void load()
	{
		try(InputStream stream = new FileInputStream(file))
		{
			recordsMeasure.clear();
			int lengthMeasure = stream.read();
			for(int i = 0; i < lengthMeasure; i++)
			{
				long time = readLong(stream);
				int temperature = stream.read();
				int humidity = stream.read();
				recordsMeasure.add(new Record(time, temperature, humidity));
			}
			
			recordsForecast.clear();
			int lengthForecast = stream.read();
			for(int i = 0; i < lengthForecast; i++)
			{
				long time = readLong(stream);
				int temperature = stream.read();
				int humidity = stream.read();
				recordsForecast.add(new Record(time, temperature, humidity));
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void save()
	{
		try(OutputStream stream = new FileOutputStream(file))
		{
			stream.write(recordsMeasure.size());
			for(Record record : recordsMeasure)
			{
				stream.write(longToBytes(record.getTime()));
				stream.write(record.getTemperature());
				stream.write(record.getHumidity());
			}
			
			stream.write(recordsForecast.size());
			for(Record record : recordsForecast)
			{
				stream.write(longToBytes(record.getTime()));
				stream.write(record.getTemperature());
				stream.write(record.getHumidity());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Record> getRecordsMeasure()
	{
		return recordsMeasure;
	}
	
	public static ArrayList<Record> getRecordsForecast()
	{
		return recordsForecast;
	}
	
	private static long readLong(InputStream stream) throws IOException
	{
		return ((stream.read() << 24) & 0xff000000) |
				((stream.read() << 16) & 0xff0000) |
				((stream.read() << 8 ) & 0xff00) |
				(stream.read()        & 0xff);
	}
	
	private static byte[] longToBytes(long number)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((number >> 24) & 0xff);
		bytes[1] = (byte) ((number >> 16) & 0xff);
		bytes[2] = (byte) ((number >> 8 ) & 0xff);
		bytes[3] = (byte)  (number        & 0xff);
		return bytes;
	}
}