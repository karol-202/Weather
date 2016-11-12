package pl.karol202.weather.record;

import java.io.File;
import java.io.IOException;
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
			if(initFile()) RecordsLoader.load(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static boolean initFile() throws IOException
	{
		file = new File("data.dat");
		return !file.createNewFile();
	}
	
	public static void save()
	{
		RecordsSaver.save(file);
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
}