package pl.karol202.weather.record;

import java.text.DateFormat;
import java.util.Date;

public class Record
{
	private static DateFormat dateFormat;
	
	private int timeInSeconds;
	private int temperature;
	private int humidity;
	
	public Record(int timeInSeconds, int temperature, int humidity)
	{
		this.timeInSeconds = timeInSeconds;
		this.temperature = temperature;
		this.humidity = humidity;
	}
	
	protected String getName()
	{
		return "Pomiar";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Record record = (Record) o;
		
		if(timeInSeconds != record.timeInSeconds) return false;
		if(temperature != record.temperature) return false;
		return humidity == record.humidity;
	}
	
	@Override
	public int hashCode()
	{
		int result = timeInSeconds ^ (timeInSeconds >>> 32);
		result = 31 * result + temperature;
		result = 31 * result + humidity;
		return result;
	}
	
	@Override
	public String toString()
	{
		if(dateFormat == null) dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date(timeInSeconds * 1000L);
		return getName() + " " + dateFormat.format(date) + "<br>" +
			   "Temperatura: " + temperature + "°C" + "<br>" +
			   "Wilgotność: " + humidity + "%" + "<br>";
	}
	
	public int getTimeInSeconds()
	{
		return timeInSeconds;
	}
	
	public int getTemperature()
	{
		return temperature;
	}
	
	public int getHumidity()
	{
		return humidity;
	}
	
	public void setTimeInSeconds(int timeInSeconds)
	{
		this.timeInSeconds = timeInSeconds;
	}
	
	public void setTemperature(int temperature)
	{
		this.temperature = temperature;
	}
	
	public void setHumidity(int humidity)
	{
		this.humidity = humidity;
	}
}