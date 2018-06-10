package pl.karol202.weather.record;

import pl.karol202.weather.hardware.DataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RecordsLoader
{
	private static InputStream inputStream;
	
	static void load(File file) throws IOException
	{
		inputStream = new FileInputStream(file);
		try
		{
			loadMeasureRecords(RecordsManager.getMeasureRecords());
			loadForecastRecords(RecordsManager.getForecastRecords());
			loadForecastSources(RecordsManager.getForecastSources());
			loadTimeZone();
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private static void loadMeasureRecords(ArrayList<MeasureRecord> records) throws IOException
	{
		records.clear();
		int length = readInt();
		for(int i = 0; i < length; i++)
		{
			int time = readInt();
			float temperature = readInt() / 10f;
			float humidity = readInt() / 10f;
			int rain = readByte();
			records.add(new MeasureRecord(time, temperature, humidity, rain));
		}
	}
	
	private static void loadForecastRecords(ArrayList<ForecastRecord> records) throws IOException
	{
		records.clear();
		int length = readInt();
		for(int i = 0; i < length; i++)
		{
			int time = readInt();
			int creationTime = readInt();
			int forecastSource = readByte();
			float temperature = readInt() / 10f;
			float humidity = readInt() / 10f;
			int rain = readByte();
			records.add(new ForecastRecord(time, creationTime, forecastSource, temperature, humidity, rain));
		}
	}
	
	private static void loadForecastSources(ArrayList<String> sources) throws IOException
	{
		sources.clear();
		int length = readByte();
		for(int i = 0; i < length; i++)
		{
			int nameLength = readByte();
			sources.add(new String(readBytes(nameLength)));
		}
	}
	
	private static void loadTimeZone() throws IOException
	{
		RecordsManager.setTimeZone(DataUtils.bytesToFloat(readBytes(4)));
	}
	
	private static int readByte() throws IOException
	{
		byte[] b = new byte[1];
		int result = inputStream.read(b);
		if(result == -1) throw new RuntimeException("File reading error: Unexpected end of stream.");
		return b[0];
	}
	
	private static byte[] readBytes(int length) throws IOException
	{
		byte[] bytes = new byte[length];
		int bytesRead = inputStream.read(bytes, 0, length);
		if(bytesRead != length) throw new RuntimeException("File reading error: Cannot read bytes array, bytes length: " + bytesRead);
		return bytes;
	}

	private static int readInt() throws IOException
	{
		byte[] buffer = new byte[4];
		int result = inputStream.read(buffer);
		if(result != 4) throw new RuntimeException("File reading error: Cannot read int, bytes length: " + result);
		return DataUtils.bytesToInt(buffer);
	}
}