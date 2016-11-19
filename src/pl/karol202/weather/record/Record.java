package pl.karol202.weather.record;

import java.text.DateFormat;
import java.util.Date;

public abstract class Record implements Comparable<MeasureRecord>
{
	private static DateFormat dateFormat;
	
	private int timeInSeconds;
	private float temperature;
	private float humidity;
	
	public Record(int timeInSeconds, float temperature, float humidity)
	{
		this.timeInSeconds = timeInSeconds;
		this.temperature = temperature;
		this.humidity = humidity;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Record record = (Record) o;
		
		if(timeInSeconds != record.timeInSeconds) return false;
		if(Float.compare(record.temperature, temperature) != 0) return false;
		return Float.compare(record.humidity, humidity) == 0;
	}
	
	@Override
	public int hashCode()
	{
		int result = timeInSeconds;
		result = 31 * result + (temperature != +0.0f ? Float.floatToIntBits(temperature) : 0);
		result = 31 * result + (humidity != +0.0f ? Float.floatToIntBits(humidity) : 0);
		return result;
	}
	
	@Override
	public String toString()
	{
		if(dateFormat == null) dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date(timeInSeconds * 1000L);
		return getName() + " " + dateFormat.format(date) + "<br>" +
			   getTemperatureString() + ": " + temperature + "°C" + "<br>" +
			   getHumidityString() + ": " + humidity + "%" + "<br>";
	}
	
	@Override
	public int compareTo(MeasureRecord o)
	{
		if(timeInSeconds < o.getTimeInSeconds()) return -1;
		else if(timeInSeconds > o.getTimeInSeconds()) return 1;
		else return 0;
	}
	
	protected abstract String getName();
	
	protected String getTemperatureString()
	{
		return "Temperatura";
	}
	
	protected String getHumidityString()
	{
		return "Wilgotność";
	}
	
	public int getTimeInSeconds()
	{
		return timeInSeconds;
	}
	
	public float getTemperature()
	{
		return temperature;
	}
	
	public float getHumidity()
	{
		return humidity;
	}
	
	public void setTimeInSeconds(int timeInSeconds)
	{
		this.timeInSeconds = timeInSeconds;
	}
	
	public void setTemperature(float temperature)
	{
		this.temperature = temperature;
	}
	
	public void setHumidity(float humidity)
	{
		this.humidity = humidity;
	}
}