package pl.karol202.weather.record;

public class MeasureRecord extends Record
{
	public static final int MAX_RAIN_LEVEL = 255;
	
	private int rainLevel;
	
	public MeasureRecord(int timeInSeconds, float temperature, float humidity, int rainLevel)
	{
		super(timeInSeconds, temperature, humidity);
		this.rainLevel = rainLevel;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		if(!super.equals(o)) return false;
		
		MeasureRecord that = (MeasureRecord) o;
		
		return rainLevel == that.rainLevel;
	}
	
	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + rainLevel;
		return result;
	}
	
	@Override
	public String toString()
	{
		float rain = rainLevel / (float) MAX_RAIN_LEVEL * 100;
		return super.toString() +
			   getRainLevelString() + ": " + rain + "%" + "<br>";
	}
	
	protected String getName()
	{
		return "Pomiar";
	}
	
	protected String getRainLevelString()
	{
		return "Moc opadów";
	}
	
	public int getRainLevel()
	{
		return rainLevel;
	}
	
	public void setRainLevel(int rainLevel)
	{
		this.rainLevel = rainLevel;
	}
}