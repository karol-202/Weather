package pl.karol202.weather.ui;

import pl.karol202.weather.record.ForecastRecord;
import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
	private int currentSourceFilter;
	private int forecastCreationTimeFilter;
	private boolean forecastCreationTimeFilterNewest;
	
	private int firstRecordTime;
	private int lastRecordTime;
	private int lowestTemperature;
	private int highestTemperature;
	private int firstVisibleTime;
	private int lastVisibleTime;
	private HashMap<Integer, ForecastRecord> newestForecastRecords;
	
	public GraphPanel()
	{
		this.formatter = DateFormat.getDateTimeInstance();
		this.showMeasurement = true;
		this.showForecast = true;
		this.showTemperature = true;
		this.showHumidity = true;
		this.daysVisible = 1;
		this.currentSourceFilter = -1;
		this.forecastCreationTimeFilterNewest = true;
		
		this.newestForecastRecords = new HashMap<>();
		
		this.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) { GraphPanel.this.onMouseMoved(e.getX()); }
		});
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
		g.drawLine(MARGIN, MARGIN, MARGIN, getHeight() - MARGIN); //Left side
		g.drawLine(getWidth() - MARGIN, MARGIN, getWidth() - MARGIN, getHeight() - MARGIN); //Right side
		g.drawLine(MARGIN, getHeight() - MARGIN, getWidth() - MARGIN, getHeight() - MARGIN); //Bottom side
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
		if(showMeasurement && RecordsManager.getMeasureRecords() != null)
			drawRecords(g, RecordsManager.getMeasureRecords(), Color.RED, Color.BLUE);
		if(showForecast && RecordsManager.getForecastRecords() != null)
			drawRecords(g, RecordsManager.getForecastRecords(), Color.ORANGE, SystemColor.CYAN);
	}
	
	private void drawRecords(Graphics2D g, ArrayList<? extends Record> records, Color colorTemperature, Color colorHumidity)
	{
		Record lastRecord = null;
		for(Record record : records)
		{
			if(!isRecordProper(record)) continue;
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
	
	private void onMouseMoved(int x)
	{
		int time = (int) map(0, getWidth(), firstVisibleTime, lastVisibleTime, x);
		
	}
	
	private ArrayList<Record> getClosestRecords(int time)
	{
		ArrayList<Record> records = new ArrayList<>();
		//for(Record record : RecordsManager.getMeasureRecords())
		return null;
	}
	
	private int compareRecords(int destination, Record closest, Record current)
	{
		int closestLength = Math.abs(closest.getTimeInSeconds() - destination);
		int currentLength = Math.abs(current.getTimeInSeconds() - destination);
		if(currentLength > closestLength) return 1;
		else if(currentLength < closestLength) return -1;
		else return 0;
	}
	
	public void updateValues()
	{
		firstRecordTime = getFirstRecordTime();
		lastRecordTime = getLastRecordTime();
		lowestTemperature = getLowestTemperature();
		highestTemperature = getHighestTemperature();
		firstVisibleTime = calcFirstVisibleTime();
		lastVisibleTime = calcLastVisbleTime();
		updateNewestRecordsMap();
		
		repaint();
	}
	
	private void updateNewestRecordsMap()
	{
		newestForecastRecords.clear();
		for(ForecastRecord record : RecordsManager.getForecastRecords())
		{
			int time = record.getTimeInSeconds();
			if(newestForecastRecords.containsKey(time))
				if(record.getCreationTimeInSeconds() >= newestForecastRecords.get(time).getCreationTimeInSeconds()) continue;
			newestForecastRecords.put(time, record);
		}
	}
	
	private int getFirstRecordTime()
	{
		Record first = null;
		if(showMeasurement)
			first = getFirstRecordFromList(RecordsManager.getMeasureRecords());
		if(showForecast)
		{
			Record firstFromForecastRecords = getFirstRecordFromList(RecordsManager.getForecastRecords());
			if(isRecordEarlier(firstFromForecastRecords, first))
				first = firstFromForecastRecords;
		}
		return first != null ? first.getTimeInSeconds() : 0;
	}
	
	private Record getFirstRecordFromList(ArrayList<? extends Record> records)
	{
		Record first = null;
		for(Record record : records)
			if(isRecordEarlierAndProper(record, first)) first = record;
		return first;
	}
	
	private boolean isRecordEarlierAndProper(Record current, Record first)
	{
		return isRecordEarlier(current, first) && isRecordProper(current);
	}
	
	private boolean isRecordEarlier(Record current, Record first)
	{
		return current != null && (first == null || current.getTimeInSeconds() < first.getTimeInSeconds());
	}
	
	private int getLastRecordTime()
	{
		Record last = null;
		if(showMeasurement)
			last = getLastRecordFromList(RecordsManager.getMeasureRecords());
		if(showForecast)
		{
			Record lastFromForecastRecords = getLastRecordFromList(RecordsManager.getForecastRecords());
			if(isRecordLater(lastFromForecastRecords, last))
				last = lastFromForecastRecords;
		}
		return last != null ? last.getTimeInSeconds() : 0;
	}
	
	private Record getLastRecordFromList(ArrayList<? extends Record> records)
	{
		Record last = null;
		for(Record record : records)
			if(isRecordLaterAndProper(record, last)) last = record;
		return last;
	}
	
	private boolean isRecordLaterAndProper(Record current, Record last)
	{
		return isRecordLater(current, last) && isRecordProper(current);
	}
	
	private boolean isRecordLater(Record current, Record last)
	{
		return current != null && (last == null || current.getTimeInSeconds() > last.getTimeInSeconds());
	}
	
	private int getLowestTemperature()
	{
		Record lowest = null;
		if(showMeasurement)
			lowest = getRecordWithLowestTemperaure(RecordsManager.getMeasureRecords());
		if(showForecast)
		{
			Record lowestFromForecastRecords = getRecordWithLowestTemperaure(RecordsManager.getForecastRecords());
			if(hasRecordLowerTemperature(lowestFromForecastRecords, lowest))
				lowest = lowestFromForecastRecords;
		}
		return lowest != null ? lowest.getTemperature() : 0;
	}
	
	private Record getRecordWithLowestTemperaure(ArrayList<? extends Record> records)
	{
		Record lowest = null;
		for(Record record : records)
			if(hasRecordLowerTemperature(record, lowest) && isRecordProper(record)) lowest = record;
		return lowest;
	}
	
	private boolean hasRecordLowerTemperature(Record current, Record lowest)
	{
		return current != null && (lowest == null || current.getTemperature() < lowest.getTemperature());
	}
	
	private int getHighestTemperature()
	{
		Record highest = null;
		if(showMeasurement)
			highest = getRecordWithHighestTemperature(RecordsManager.getMeasureRecords());
		if(showForecast)
		{
			Record highestFromForecastRecords = getRecordWithHighestTemperature(RecordsManager.getForecastRecords());
			if(hasRecordHigherTemperature(highestFromForecastRecords, highest))
				highest = highestFromForecastRecords;
		}
		return highest != null ? highest.getTemperature() : 1;
	}
	
	private Record getRecordWithHighestTemperature(ArrayList<? extends Record> records)
	{
		Record highest = null;
		for(Record record : records)
			if(hasRecordHigherTemperature(record, highest) && isRecordProper(record)) highest = record;
		return highest;
	}
	
	private boolean hasRecordHigherTemperature(Record current, Record highest)
	{
		return current != null && (highest == null || current.getTemperature() > highest.getTemperature());
	}
	
	private boolean isRecordProper(Record record)
	{
		if(record == null) return false;
		if(!(record instanceof ForecastRecord)) return true;
		ForecastRecord fr = (ForecastRecord) record;
		if(fr.getForecastSource() != currentSourceFilter) return false;
		if(forecastCreationTimeFilterNewest)
		{
			if(!newestForecastRecords.containsValue(fr)) return false;
		}
		else
			if(fr.getCreationTimeInSeconds() != forecastCreationTimeFilter) return false;
		return true;
	}
	
	private int calcFirstVisibleTime()
	{
		return firstRecordTime + Math.round(((lastRecordTime - firstRecordTime) - (SECONDS_IN_DAY * daysVisible)) * (offsetPercent / 100f));
	}
	
	private int calcLastVisbleTime()
	{
		return firstVisibleTime + (SECONDS_IN_DAY * daysVisible);
	}
	
	public int getTimeScaleRatio()
	{
		return Math.round((float) daysVisible / ((lastRecordTime - firstRecordTime) / SECONDS_IN_DAY) * 100);
	}
	
	public void setShowMeasurement(boolean showMeasurement)
	{
		this.showMeasurement = showMeasurement;
	}
	
	public void setShowForecast(boolean showForecast)
	{
		this.showForecast = showForecast;
	}
	
	public void setShowTemperature(boolean showTemperature)
	{
		this.showTemperature = showTemperature;
	}
	
	public void setShowHumidity(boolean showHumidity)
	{
		this.showHumidity = showHumidity;
	}
	
	public void setDaysVisible(int daysVisible)
	{
		this.daysVisible = daysVisible;
	}
	
	public void setOffsetPercent(int offsetPercent)
	{
		this.offsetPercent = offsetPercent;
	}
	
	public void setCurrentSourceFilter(int currentSourceFilter)
	{
		this.currentSourceFilter = currentSourceFilter;
	}
	
	public void setForecastCreationTimeFilter(int time)
	{
		this.forecastCreationTimeFilter = time;
	}
	
	public void setForecastCreationTimeFilterNewest(boolean newest)
	{
		this.forecastCreationTimeFilterNewest = newest;
	}
}