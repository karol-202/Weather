package pl.karol202.weather.record;

public class ForecastErrorRecord extends Record
{
	public ForecastErrorRecord(int timeInSeconds, int temperature, int humidity)
	{
		super(timeInSeconds, temperature, humidity);
	}
	
	@Override
	protected String getName()
	{
		return "Błąd prognozy";
	}
	
	@Override
	protected String getTemperatureString()
	{
		return "Różnica temperatury";
	}
	
	@Override
	protected String getHumidityString()
	{
		return "Różnica wilgotności";
	}
}