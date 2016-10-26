package pl.karol202.weather;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraphPanel extends JPanel
{
	private final int MARGIN = 30;
	
	private int startTime;
	private int endTime;
	private int startTemperature;
	private int endTemperature;
	
	private boolean showMeasurement;
	private boolean showForecast;
	private boolean showTemperature;
	private boolean showHumidity;
	
	public GraphPanel()
	{
		this.showMeasurement = true;
		this.showForecast = true;
		this.showTemperature = true;
		this.showHumidity = true;
		updateValues();
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D graphics = (Graphics2D) g;
		
		drawGrid(graphics);
		drawData(graphics);
	}
	
	private void drawGrid(Graphics2D g)
	{
		g.setColor(Color.black);
		g.drawLine(MARGIN, MARGIN, MARGIN, getHeight() - MARGIN);
		g.drawLine(MARGIN, getHeight() - MARGIN, getWidth() - MARGIN, getHeight() - MARGIN);
	}
	
	private void drawData(Graphics2D g)
	{
		if(showMeasurement) drawRecords(g, RecordsManager.getRecordsMeasure(), Color.RED, Color.BLUE);
		if(showForecast) drawRecords(g, RecordsManager.getRecordsForecast(), Color.ORANGE, SystemColor.CYAN);
	}
	
	private void drawRecords(Graphics2D g, ArrayList<Record> records, Color colorTemperature, Color colorHumidity)
	{
		Record lastRecord = null;
		for(Record record : records)
		{
			if(lastRecord != null)
			{
				int x = (int) map(startTime, endTime, MARGIN, getWidth() - MARGIN, record.getTimeInSeconds());
				int lastX = (int) map(startTime, endTime, MARGIN, getWidth() - MARGIN, lastRecord.getTimeInSeconds());
				if(showTemperature)
				{
					int y = (int) map(startTemperature, endTemperature, getHeight() - MARGIN, MARGIN, record.getTemperature());
					int lastY = (int) map(startTemperature, endTemperature, getHeight() - MARGIN, MARGIN, lastRecord.getTemperature());
					g.setColor(colorTemperature);
					g.drawLine(lastX, lastY, x, y);
				}
				if(showHumidity)
				{
					int y = (int) map(0, 100, getHeight() - MARGIN, MARGIN, record.getHumidity());
					int lastY = (int) map(0, 100, getHeight() - MARGIN, MARGIN, lastRecord.getHumidity());
					g.setColor(colorHumidity);
					g.drawLine(lastX, lastY, x, y);
				}
			}
			lastRecord = record;
		}
	}
	
	private float map(int srcMin, int srcMax, int dstMin, int dstMax, float value)
	{
		return (value - srcMin) / (srcMax - srcMin) * (dstMax - dstMin) + dstMin;
	}
	
	public void updateValues()
	{
		this.startTime = getFirstRecordTime();
		this.endTime = getLastRecordTime();
		this.startTemperature = getSmallestTemperature();
		this.endTemperature = getLargestTemperature();
		repaint();
	}
	
	private int getFirstRecordTime()
	{
		int first = -1;
		if(showMeasurement) for(Record record : RecordsManager.getRecordsMeasure())
			if(first == -1 || record.getTimeInSeconds() < first) first = record.getTimeInSeconds();
		if(showForecast) for(Record record : RecordsManager.getRecordsForecast())
			if(first == -1 || record.getTimeInSeconds() < first) first = record.getTimeInSeconds();
		return first;
	}
	
	private int getLastRecordTime()
	{
		int last = -1;
		if(showMeasurement) for(Record record : RecordsManager.getRecordsMeasure())
			if(last == -1 || record.getTimeInSeconds() > last) last = record.getTimeInSeconds();
		if(showForecast) for(Record record : RecordsManager.getRecordsForecast())
			if(last == -1 || record.getTimeInSeconds() > last) last = record.getTimeInSeconds();
		return last;
	}
	
	private int getSmallestTemperature()
	{
		int small = Integer.MIN_VALUE;
		if(showMeasurement) for(Record record : RecordsManager.getRecordsMeasure())
			if(small == Integer.MIN_VALUE || record.getTemperature() < small) small = record.getTemperature();
		if(showForecast) for(Record record : RecordsManager.getRecordsForecast())
			if(small == Integer.MIN_VALUE || record.getTemperature() < small) small = record.getTemperature();
		return small;
	}
	
	private int getLargestTemperature()
	{
		int large = Integer.MIN_VALUE;
		if(showMeasurement) for(Record record : RecordsManager.getRecordsMeasure())
			if(large == Integer.MIN_VALUE || record.getTemperature() > large) large = record.getTemperature();
		if(showForecast) for(Record record : RecordsManager.getRecordsForecast())
			if(large == Integer.MIN_VALUE || record.getTemperature() > large) large = record.getTemperature();
		return large;
	}
	
	public boolean isShowMeasurement()
	{
		return showMeasurement;
	}
	
	public void setShowMeasurement(boolean showMeasurement)
	{
		this.showMeasurement = showMeasurement;
		updateValues();
	}
	
	public boolean isShowForecast()
	{
		return showForecast;
	}
	
	public void setShowForecast(boolean showForecast)
	{
		this.showForecast = showForecast;
		updateValues();
	}
	
	public boolean isShowTemperature()
	{
		return showTemperature;
	}
	
	public void setShowTemperature(boolean showTemperature)
	{
		this.showTemperature = showTemperature;
		updateValues();
	}
	
	public boolean isShowHumidity()
	{
		return showHumidity;
	}
	
	public void setShowHumidity(boolean showHumidity)
	{
		this.showHumidity = showHumidity;
		updateValues();
	}
}