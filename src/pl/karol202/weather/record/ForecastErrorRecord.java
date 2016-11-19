package pl.karol202.weather.record;

public class ForecastErrorRecord extends MeasureRecord
{
	public ForecastErrorRecord(int timeInSeconds, float temperature, float humidity)
	{
		super(timeInSeconds, temperature, humidity, 0);
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