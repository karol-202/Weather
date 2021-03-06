package pl.karol202.weather.record;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordsManager
{
	private static ArrayList<MeasureRecord> measureRecords;
	private static ArrayList<ForecastRecord> forecastRecords;
	private static ArrayList<String> forecastSources;
	private static float timeZone;
	
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
		String path = System.getProperty("user.home") + "/.Weather/data.dat";
		file = new File(path);
		return !file.createNewFile();
	}
	
	public static void save()
	{
		RecordsSaver.save(file);
	}
	
	public static ArrayList<MeasureRecord> getMeasureRecords()
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
	
	public static float getTimeZone()
	{
		return timeZone;
	}
	
	public static void setTimeZone(float timeZone)
	{
		RecordsManager.timeZone = timeZone;
	}
}