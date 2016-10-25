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
			loadRecordsToList(stream, recordsMeasure);
			loadRecordsToList(stream, recordsForecast);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void loadRecordsToList(InputStream stream, ArrayList<Record> records) throws IOException
	{
		records.clear();
		int lengthMeasure = stream.read();
		for(int i = 0; i < lengthMeasure; i++)
		{
			int time = DataUtils.bytesToInt(stream);
			int temperature = stream.read();
			int humidity = stream.read();
			records.add(new Record(time, temperature, humidity));
		}
	}
	
	public static void save()
	{
		try(OutputStream stream = new FileOutputStream(file))
		{
			saveRecordsToList(stream, recordsMeasure);
			saveRecordsToList(stream, recordsForecast);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void saveRecordsToList(OutputStream stream, ArrayList<Record> records) throws IOException
	{
		stream.write(records.size());
		for(Record record : records)
		{
			stream.write(DataUtils.intToBytes(record.getTimeInSeconds()));
			stream.write(record.getTemperature());
			stream.write(record.getHumidity());
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
}