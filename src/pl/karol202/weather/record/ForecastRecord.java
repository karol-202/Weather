package pl.karol202.weather.record;

public class ForecastRecord extends Record
{
	private int creationTimeInSeconds;
	private int forecastSource;
	
	public ForecastRecord(int timeInSeconds, int creationTimeInSeconds, int forecastSource, int temperature, int humidity)
	{
		super(timeInSeconds, temperature, humidity);
		this.creationTimeInSeconds = creationTimeInSeconds;
		this.forecastSource = forecastSource;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		if(!super.equals(o)) return false;
		
		ForecastRecord that = (ForecastRecord) o;
		
		if(creationTimeInSeconds != that.creationTimeInSeconds) return false;
		return forecastSource == that.forecastSource;
		
	}
	
	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + creationTimeInSeconds;
		result = 31 * result + forecastSource;
		return result;
	}
	
	public int getCreationTimeInSeconds()
	{
		return creationTimeInSeconds;
	}
	
	public void setCreationTimeInSeconds(int creationTimeInSeconds)
	{
		this.creationTimeInSeconds = creationTimeInSeconds;
	}
	
	public int getForecastSource()
	{
		return forecastSource;
	}
	
	public void setForecastSource(int forecastSource)
	{
		this.forecastSource = forecastSource;
	}
}
