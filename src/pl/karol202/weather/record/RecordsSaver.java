package pl.karol202.weather.record;

import pl.karol202.weather.hardware.DataUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class RecordsSaver
{
	private static OutputStream outputStream;
	
	static void save(File file)
	{
		try
		{
			outputStream = new FileOutputStream(file);
			saveMeasureRecords(RecordsManager.getMeasureRecords());
			saveForecastRecords(RecordsManager.getForecastRecords());
			saveForecastSources(RecordsManager.getForecastSources());
			saveTimeZone();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeOutputStream();
		}
	}
	
	private static void saveMeasureRecords(ArrayList<MeasureRecord> records) throws IOException
	{
		outputStream.write(DataUtils.intToBytes(records.size()));
		for(MeasureRecord record : records)
		{
			outputStream.write(DataUtils.intToBytes(record.getTimeInSeconds()));
			outputStream.write(DataUtils.intToBytes((int) (record.getTemperature() * 10)));
			outputStream.write(DataUtils.intToBytes((int) (record.getHumidity() * 10)));
			outputStream.write(record.getRainLevel());
		}
	}
	
	private static void saveForecastRecords(ArrayList<ForecastRecord> records) throws IOException
	{
		outputStream.write(DataUtils.intToBytes(records.size()));
		for(ForecastRecord record : records)
		{
			outputStream.write(DataUtils.intToBytes(record.getTimeInSeconds()));
			outputStream.write(DataUtils.intToBytes(record.getCreationTimeInSeconds()));
			outputStream.write(record.getForecastSource());
			outputStream.write(DataUtils.intToBytes((int) (record.getTemperature() * 10)));
			outputStream.write(DataUtils.intToBytes((int) (record.getHumidity() * 10)));
			outputStream.write(record.getRainProbability());
		}
	}
	
	private static void saveForecastSources(ArrayList<String> sources) throws IOException
	{
		outputStream.write(sources.size());
		for(String name : sources)
		{
			outputStream.write(name.length());
			outputStream.write(name.getBytes());
		}
	}
	
	private static void saveTimeZone() throws IOException
	{
		outputStream.write(DataUtils.floatToBytes(RecordsManager.getTimeZone()));
	}
	
	private static void closeOutputStream()
	{
		try
		{
			outputStream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}