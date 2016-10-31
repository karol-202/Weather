package pl.karol202.weather.ui;

import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GraphPanel extends JPanel
{
	private final int MARGIN = 30;
	private final int SECONDS_IN_DAY = 60 * 60 * 24;
	
	private DateFormat formatter;
	private boolean showMeasurement;
	private boolean showForecast;
	private boolean showTemperature;
	private boolean showHumidity;
	private int daysVisible;
	private int offsetPercent;
	
	private int firstRecordTime;
	private int lastRecordTime;
	private int lowestTemperature;
	private int highestTemperature;
	
	private int firstVisibleTime;
	private int lastVisibleTime;
	
	public GraphPanel()
	{
		this.formatter = DateFormat.getDateTimeInstance();
		this.showMeasurement = true;
		this.showForecast = true;
		this.showTemperature = true;
		this.showHumidity = true;
		this.daysVisible = 1;
		this.offsetPercent = 0;
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D graphics = (Graphics2D) g;
		
		drawGrid(graphics);
		drawTexts(graphics);
		g.clipRect(MARGIN, MARGIN, getWidth() - (2 * MARGIN), getHeight() - (2 * MARGIN));
		drawData(graphics);
	}
	
	private void drawGrid(Graphics2D g)
	{
		g.setColor(Color.BLACK);
		g.drawLine(MARGIN, MARGIN, MARGIN, getHeight() - MARGIN);
		g.drawLine(MARGIN, getHeight() - MARGIN, getWidth() - MARGIN, getHeight() - MARGIN);
		g.drawLine(getWidth() - MARGIN, MARGIN, getWidth() - MARGIN, getHeight() - MARGIN);
	}
	
	private void drawTexts(Graphics2D g)
	{
		g.setColor(Color.RED);
		String startTemperatureText = Integer.toString(lowestTemperature) + "°C";
		String endTemperatureText = Integer.toString(highestTemperature) + "°C";
		g.drawString(startTemperatureText, MARGIN - (startTemperatureText.length() * 6), getHeight() - MARGIN + 2);
		g.drawString(endTemperatureText, MARGIN - (endTemperatureText.length() * 6), MARGIN + 2);
		
		g.setColor(Color.BLUE);
		g.drawString("0%", getWidth() - MARGIN + 2, getHeight() - MARGIN + 2);
		g.drawString("100%", getWidth() - MARGIN + 2, MARGIN + 2);
		
		g.setColor(Color.BLACK);
		String startTimeText = formatter.format(new Date((long) firstVisibleTime * 1000));
		String endTimeText = formatter.format(new Date((long) lastVisibleTime * 1000));
		g.drawString(startTimeText, MARGIN - 13, getHeight() - MARGIN + 14);
		g.drawString(endTimeText, getWidth() - MARGIN - 80, getHeight() - MARGIN + 14);
	}
	
	private void drawData(Graphics2D g)
	{
		if(showMeasurement && RecordsManager.getMeasureRecords() != null) drawRecords(g, RecordsManager.getMeasureRecords(), Color.RED, Color.BLUE);
		if(showForecast && RecordsManager.getForecastRecords() != null) drawRecords(g, RecordsManager.getForecastRecords(), Color.ORANGE, SystemColor.CYAN);
	}
	
	private void drawRecords(Graphics2D g, ArrayList<? extends Record> records, Color colorTemperature, Color colorHumidity)
	{
		Record lastRecord = null;
		for(Record record : records)
		{
			if(lastRecord != null)
			{
				int x = (int) map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, record.getTimeInSeconds());
				int lastX = (int) map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, lastRecord.getTimeInSeconds());
				if(showTemperature)
				{
					int y = (int) map(lowestTemperature, highestTemperature, getHeight() - MARGIN, MARGIN, record.getTemperature());
					int lastY = (int) map(lowestTemperature, highestTemperature, getHeight() - MARGIN, MARGIN, lastRecord.getTemperature());
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
		firstRecordTime = getFirstRecordTime();
		lastRecordTime = getLastRecordTime();
		lowestTemperature = getLowestTemperature();
		highestTemperature = getHighestTemperature();
		
		firstVisibleTime = firstRecordTime + Math.round(((lastRecordTime - firstRecordTime) - (SECONDS_IN_DAY * daysVisible)) * (offsetPercent / 100f));
		lastVisibleTime = firstVisibleTime + (SECONDS_IN_DAY * daysVisible);
		repaint();
	}
	
	private int getFirstRecordTime()
	{
		int first = -1;
		if(showMeasurement)
			for(Record record : RecordsManager.getMeasureRecords())
				if(first == -1 || record.getTimeInSeconds() < first) first = record.getTimeInSeconds();
		if(showForecast)
			for(Record record : RecordsManager.getForecastRecords())
				if(first == -1 || record.getTimeInSeconds() < first) first = record.getTimeInSeconds();
		return first;
	}
	
	private int getLastRecordTime()
	{
		int last = -1;
		if(showMeasurement)
			for(Record record : RecordsManager.getMeasureRecords())
				if(last == -1 || record.getTimeInSeconds() > last) last = record.getTimeInSeconds();
		if(showForecast)
			for(Record record : RecordsManager.getForecastRecords())
				if(last == -1 || record.getTimeInSeconds() > last) last = record.getTimeInSeconds();
		return last;
	}
	
	private int getLowestTemperature()
	{
		int small = Integer.MIN_VALUE;
		if(showMeasurement)
			for(Record record : RecordsManager.getMeasureRecords())
				if(small == Integer.MIN_VALUE || record.getTemperature() < small) small = record.getTemperature();
		if(showForecast)
			for(Record record : RecordsManager.getForecastRecords())
				if(small == Integer.MIN_VALUE || record.getTemperature() < small) small = record.getTemperature();
		return small;
	}
	
	private int getHighestTemperature()
	{
		int large = Integer.MIN_VALUE;
		if(showMeasurement)
			for(Record record : RecordsManager.getMeasureRecords())
				if(large == Integer.MIN_VALUE || record.getTemperature() > large) large = record.getTemperature();
		if(showForecast)
			for(Record record : RecordsManager.getForecastRecords())
				if(large == Integer.MIN_VALUE || record.getTemperature() > large) large = record.getTemperature();
		return large;
	}
	
	public int getTimeScaleRatio()
	{
		return Math.round((float) daysVisible / ((lastRecordTime - firstRecordTime) / SECONDS_IN_DAY) * 100);
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
	
	public int getDaysVisible()
	{
		return daysVisible;
	}
	
	public void setDaysVisible(int daysVisible)
	{
		this.daysVisible = daysVisible;
		updateValues();
	}
	
	public int getOffsetPercent()
	{
		return offsetPercent;
	}
	
	public void setOffsetPercent(int offsetPercent)
	{
		this.offsetPercent = offsetPercent;
		updateValues();
	}
}