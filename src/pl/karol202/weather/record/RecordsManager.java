package pl.karol202.weather.record;

import pl.karol202.weather.hardware.DataUtils;

import java.io.*;
import java.util.ArrayList;

public class RecordsManager
{
	private static ArrayList<Record> measureRecords;
	private static ArrayList<ForecastRecord> forecastRecords;
	private static ArrayList<String> forecastSources;
	private static File file;
	
	public static void init()
	{
		measureRecords = new ArrayList<>();
		forecastRecords = new ArrayList<>();
		forecastSources = new ArrayList<>();
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
			loadMeasureRecords(stream, measureRecords);
			loadForecastRecords(stream, forecastRecords);
			loadForecastSources(stream, forecastSources);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void loadMeasureRecords(InputStream stream, ArrayList<Record> records) throws IOException
	{
		records.clear();
		int length = DataUtils.bytesToInt(stream);
		for(int i = 0; i < length; i++)
		{
			int time = DataUtils.bytesToInt(stream);
			int temperature = readByte(stream);
			int humidity = readByte(stream);
			records.add(new Record(time, temperature, humidity));
		}
	}
	
	private static void loadForecastRecords(InputStream stream, ArrayList<ForecastRecord> records) throws IOException
	{
		records.clear();
		int length = DataUtils.bytesToInt(stream);
		for(int i = 0; i < length; i++)
		{
			int time = DataUtils.bytesToInt(stream);
			int creationTime = DataUtils.bytesToInt(stream);
			//int forecastSource = readByte(stream);
			int temperature = readByte(stream);
			int humidity = readByte(stream);
			records.add(new ForecastRecord(time, creationTime, 0, temperature, humidity));
		}
	}
	
	private static void loadForecastSources(InputStream stream, ArrayList<String> sources) throws IOException
	{
		sources.clear();
		int length = readByte(stream);
		for(int i = 0; i < length; i++)
		{
			int nameLength = readByte(stream);
			sources.add(new String(readBytes(stream, nameLength)));
		}
	}
	
	private static int readByte(InputStream stream) throws IOException
	{
		int data = stream.read();
		if(data == -1) throw new RuntimeException("File reading error: Unexpected end of stream.");
		return data;
	}
	
	private static byte[] readBytes(InputStream stream, int length) throws IOException
	{
		byte[] bytes = new byte[length];
		int bytesRead = stream.read(bytes, 0, length);
		if(bytesRead != length) throw new RuntimeException("File reading error: Cannot read bytes array, bytes length: " + bytesRead);
		return bytes;
	}
	
	public static void save()
	{
		try(OutputStream stream = new FileOutputStream(file))
		{
			saveMeasureRecords(stream, measureRecords);
			saveForecastRecords(stream, forecastRecords);
			saveForecastSources(stream, forecastSources);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void saveMeasureRecords(OutputStream stream, ArrayList<Record> records) throws IOException
	{
		stream.write(DataUtils.intToBytes(records.size()));
		for(Record record : records)
		{
			stream.write(DataUtils.intToBytes(record.getTimeInSeconds()));
			stream.write(record.getTemperature());
			stream.write(record.getHumidity());
		}
	}
	
	private static void saveForecastRecords(OutputStream stream, ArrayList<ForecastRecord> records) throws IOException
	{
		stream.write(DataUtils.intToBytes(records.size()));
		for(ForecastRecord record : records)
		{
			stream.write(DataUtils.intToBytes(record.getTimeInSeconds()));
			stream.write(DataUtils.intToBytes(record.getCreationTimeInSeconds()));
			//stream.write(record.getForecastSource());
			stream.write(record.getTemperature());
			stream.write(record.getHumidity());
		}
	}
	
	private static void saveForecastSources(OutputStream stream, ArrayList<String> sources) throws IOException
	{
		stream.write(sources.size());
		for(String name : sources)
		{
			stream.write(name.length());
			stream.write(name.getBytes());
		}
	}
	
	public static ArrayList<Record> getMeasureRecords()
	{
		return measureRecords;
	}
	
	public static ArrayList<ForecastRecord> getForecastRecords()
	{
		return forecastRecords;
	}
	
	public static ArrayList<String> getForecastSources()
	{
		return forecastSources;
	}
	
	public static int getForecastSourceId(String source)
	{
		for(int i = 0; i < forecastSources.size(); i++)
		{
			if(source.equals(forecastSources.get(i))) return i;
		}
		return -1;
	}
}