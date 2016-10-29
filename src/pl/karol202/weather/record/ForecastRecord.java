package pl.karol202.weather.record;

public class ForecastRecord extends Record
{
	private int creationTimeInSeconds;
	
	public ForecastRecord(int timeInSeconds, int creationTimeInSeconds, int temperature, int humidity)
	{
		super(timeInSeconds, temperature, humidity);
		this.creationTimeInSeconds = creationTimeInSeconds;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		if(!super.equals(o)) return false;
		
		ForecastRecord that = (ForecastRecord) o;
		
		return creationTimeInSeconds == that.creationTimeInSeconds;
	}
	
	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + creationTimeInSeconds;
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
}
