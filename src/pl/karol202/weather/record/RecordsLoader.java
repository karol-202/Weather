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
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private static void loadMeasureRecords(ArrayList<Record> records) throws IOException
	{
		records.clear();
		int length = DataUtils.bytesToInt(inputStream);
		for(int i = 0; i < length; i++)
		{
			int time = DataUtils.bytesToInt(inputStream);
			float temperature = DataUtils.bytesToInt(inputStream) / 10f;
			float humidity = DataUtils.bytesToInt(inputStream) / 10f;
			records.add(new Record(time, temperature, humidity));
		}
	}
	
	private static void loadForecastRecords(ArrayList<ForecastRecord> records) throws IOException
	{
		records.clear();
		int length = DataUtils.bytesToInt(inputStream);
		for(int i = 0; i < length; i++)
		{
			int time = DataUtils.bytesToInt(inputStream);
			int creationTime = DataUtils.bytesToInt(inputStream);
			int forecastSource = readByte();
			float temperature = DataUtils.bytesToInt(inputStream) / 10f;
			float humidity = DataUtils.bytesToInt(inputStream) / 10f;
			records.add(new ForecastRecord(time, creationTime, forecastSource, temperature, humidity));
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
}