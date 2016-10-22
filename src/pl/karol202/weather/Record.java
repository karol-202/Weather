package pl.karol202.weather;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Record
{
	private long time;
	private int temperature;
	private int humidity;
	
	public Record(long time, int temperature, int humidity)
	{
		this.time = time;
		this.temperature = temperature;
		this.humidity = humidity;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Record record = (Record) o;
		
		if(time != record.time) return false;
		if(temperature != record.temperature) return false;
		return humidity == record.humidity;
	}
	
	@Override
	public int hashCode()
	{
		int result = (int) (time ^ (time >>> 32));
		result = 31 * result + temperature;
		result = 31 * result + humidity;
		return result;
	}
	
	public long getTime()
	{
		return time;
	}
	
	public int getTemperature()
	{
		return temperature;
	}
	
	public int getHumidity()
	{
		return humidity;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}
	
	public void setTemperature(int temperature)
	{
		this.temperature = temperature;
	}
	
	public void setHumidity(int humidity)
	{
		this.humidity = humidity;
	}
	
	public static ArrayList<Record> decodeRecord(InputStream stream) throws IOException
	{
		ArrayList<Record> records = new ArrayList<>();
		int length = stream.read();
		System.out.println(length);
		for(int i = 0; i < length; i++)
		{
			long time = readLong(stream) * 1000;
			System.out.println(time / 1000);
			int temperature = stream.read();
			int humidity = stream.read();
			records.add(new Record(time, temperature, humidity));
		}
		return records;
	}
	
	private static long readLong(InputStream stream) throws IOException
	{
		int[] b = new int[4];
		b[0] = stream.read();
		b[1] = stream.read();
		b[2] = stream.read();
		b[3] = stream.read();
		System.out.println(b[0]);
		System.out.println(b[1]);
		System.out.println(b[2]);
		System.out.println(b[3]);
		return ((b[0] << 24) & 0xff000000) |
			   ((b[1] << 16) & 0xff0000) |
			   ((b[2] << 8 ) & 0xff00) |
			    (b[3]        & 0xff);
	}
}