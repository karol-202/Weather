package pl.karol202.weather.record;

public class ForecastRecord extends Record
{
	private int creationTimeInSeconds;
	private int forecastSource;
	private int rainProbability;
	
	public ForecastRecord(int timeInSeconds, int creationTimeInSeconds, int forecastSource,
	                      float temperature, float humidity, int rainProbability)
	{
		super(timeInSeconds, temperature, humidity);
		this.creationTimeInSeconds = creationTimeInSeconds;
		this.forecastSource = forecastSource;
		this.rainProbability = rainProbability;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		if(!super.equals(o)) return false;
		
		ForecastRecord that = (ForecastRecord) o;
		
		if(creationTimeInSeconds != that.creationTimeInSeconds) return false;
		if(forecastSource != that.forecastSource) return false;
		return rainProbability == that.rainProbability;
	}
	
	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + creationTimeInSeconds;
		result = 31 * result + forecastSource;
		result = 31 * result + rainProbability;
		return result;
	}
	
	@Override
	public String toString()
	{
		return super.toString() +
				getRainProbabilityString() + ": " + rainProbability + "%" + "<br>";
	}
	
	@Override
	protected String getName()
	{
		return "Prognoza";
	}
	
	private String getRainProbabilityString()
	{
		return "Prawdop. deszczu";
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
	
	public int getRainProbability()
	{
		return rainProbability;
	}
	
	public void setRainProbability(int rainProbability)
	{
		this.rainProbability = rainProbability;
	}
}
