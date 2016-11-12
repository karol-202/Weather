package pl.karol202.weather.ui;

import pl.karol202.weather.record.ForecastErrorRecord;
import pl.karol202.weather.record.ForecastRecord;
import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.*;

public class GraphPanel extends JPanel
{
	private final int MARGIN = 30;
	private final int SECONDS_IN_DAY = 60 * 60 * 24;
	private final int MAX_HIGHLIGHT_DISTANCE = 20;
	
	private DateFormat formatter;
	private boolean showMeasurement;
	private boolean showForecast;
	private boolean showForecastError;
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
	private int lowestHumidity;
	private int highestHumidity;
	private int firstVisibleTime;
	private int lastVisibleTime;
	private HashMap<Integer, ForecastRecord> newestForecastRecords;
	private ArrayList<Record> highlightedRecords;
	private int mouseX;
	private ArrayList<Record> forecastErrorData;
	
	public GraphPanel()
	{
		this.formatter = DateFormat.getDateTimeInstance();
		this.showMeasurement = true;
		this.showForecast = true;
		this.showForecastError = false;
		this.showTemperature = true;
		this.showHumidity = true;
		this.daysVisible = 1;
		this.currentSourceFilter = -1;
		this.forecastCreationTimeFilterNewest = true;
		
		this.newestForecastRecords = new HashMap<>();
		this.highlightedRecords = new ArrayList<>();
		this.forecastErrorData = new ArrayList<>();
		
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e)
			{
				GraphPanel.this.onMouseExited();
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				GraphPanel.this.onMouseMoved(e.getX());
			}
			
			@Override
			public void mouseDragged(MouseEvent e)
			{
				GraphPanel.this.onMouseMoved(e.getX());
			}
		};
		this.addMouseListener(adapter);
		this.addMouseMotionListener(adapter);
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D graphics = (Graphics2D) g;
		
		drawGrid(graphics);
		drawTexts(graphics);
		g.clipRect(MARGIN, MARGIN, getWidth() - (2 * MARGIN), getHeight() - (2 * MARGIN));
		drawHighlight(graphics);
		drawData(graphics);
	}
	
	private void drawGrid(Graphics2D g)
	{
		g.setColor(Color.BLACK);
		g.drawLine(MARGIN, MARGIN, MARGIN, getHeight() - MARGIN); //Left side
		g.drawLine(getWidth() - MARGIN, MARGIN, getWidth() - MARGIN, getHeight() - MARGIN); //Right side
		if(!showForecastError) g.drawLine(MARGIN, getHeight() - MARGIN, getWidth() - MARGIN, getHeight() - MARGIN); //Bottom side
		else g.drawLine(MARGIN, getHeight() / 2, getWidth() - MARGIN, getHeight() / 2); //Center line
	}
	
	private void drawTexts(Graphics2D g)
	{
		drawTextsTemperature(g);
		drawTextsHumidity(g);
		drawTextsTime(g);
	}
	
	private void drawTextsTemperature(Graphics2D g)
	{
		g.setColor(Color.RED);
		String startTemperatureText = Integer.toString(lowestTemperature) + "°C";
		String endTemperatureText = Integer.toString(highestTemperature) + "°C";
		g.drawString(startTemperatureText, MARGIN - (startTemperatureText.length() * 6), getHeight() - MARGIN + 2);
		g.drawString(endTemperatureText, MARGIN - (endTemperatureText.length() * 6), MARGIN + 2);
		if(showForecastError) g.drawString("0°C", MARGIN - 18, (getHeight() / 2) + 2);
	}
	
	private void drawTextsHumidity(Graphics2D g)
	{
		g.setColor(Color.BLUE);
		if(showForecastError)
		{
			g.drawString("-100%", getWidth() - MARGIN, getHeight() - MARGIN + 2);
			g.drawString("0%", getWidth() - MARGIN + 2, (getHeight() / 2) + 2);
			g.drawString("100%", getWidth() - MARGIN + 2, MARGIN + 2);
		}
		else
		{
			g.drawString("0%", getWidth() - MARGIN + 2, getHeight() - MARGIN + 2);
			g.drawString("100%", getWidth() - MARGIN + 2, MARGIN + 2);
		}
	}
	
	private void drawTextsTime(Graphics2D g)
	{
		g.setColor(Color.BLACK);
		String startTimeText = formatter.format(new Date((long) firstVisibleTime * 1000));
		String endTimeText = formatter.format(new Date((long) lastVisibleTime * 1000));
		g.drawString(startTimeText, MARGIN - 13, getHeight() - MARGIN + 14);
		g.drawString(endTimeText, getWidth() - MARGIN - 80, getHeight() - MARGIN + 14);
	}
	
	private void drawHighlight(Graphics2D g)
	{
		if(highlightedRecords.size() == 0) return;
		int highlightedTime = highlightedRecords.get(0).getTimeInSeconds();
		int x = Math.round(map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, highlightedTime));
		if(Math.abs(x - mouseX) > MAX_HIGHLIGHT_DISTANCE) return;
		g.setColor(Color.GRAY);
		g.drawLine(x, MARGIN, x, getHeight() - MARGIN);
	}
	
	private void drawData(Graphics2D g)
	{
		if(showForecastError) drawDataError(g);
		else drawDataStandard(g);
	}
	
	private void drawDataError(Graphics2D g)
	{
		drawRecords(g, forecastErrorData, Color.RED, Color.BLUE);
	}
	
	private void drawDataStandard(Graphics2D g)
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
					int y = (int) map(lowestHumidity, highestHumidity, getHeight() - MARGIN, MARGIN, record.getHumidity());
					int lastY = (int) map(lowestHumidity, highestHumidity, getHeight() - MARGIN, MARGIN, lastRecord.getHumidity());
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
	
	private void onMouseExited()
	{
		highlightedRecords.clear();
		repaint();
	}
	
	private void onMouseMoved(int x)
	{
		mouseX = x;
		int time = (int) map(MARGIN, getWidth() - MARGIN, firstVisibleTime, lastVisibleTime, x);
		updateHighlightedRecords(time);
		updateToolTip();
	}
	
	private void updateHighlightedRecords(int mouseTime)
	{
		highlightedRecords.clear();
		if(showForecastError)
			for(Record record : forecastErrorData) checkRecord(mouseTime, record);
		else
		{
			if(showMeasurement) for(Record record : RecordsManager.getMeasureRecords()) checkRecord(mouseTime, record);
			if(showForecast) for(Record record : RecordsManager.getForecastRecords()) checkRecord(mouseTime, record);
		}
		repaint();
	}
	
	private void checkRecord(int destination, Record current)
	{
		if(!isRecordProper(current)) return;
		
		int comparision;
		if(highlightedRecords.size() == 0) comparision = 1;
		else comparision = compareRecords(destination, highlightedRecords.get(0), current);
		
		if(comparision == 1)
		{
			highlightedRecords.clear();
			highlightedRecords.add(current);
		}
		else if(comparision == 0) highlightedRecords.add(current);
	}
	
	private int compareRecords(int destination, Record closest, Record current)
	{
		int closestLength = Math.abs(closest.getTimeInSeconds() - destination);
		int currentLength = Math.abs(current.getTimeInSeconds() - destination);
		if(currentLength < closestLength) return 1;
		else if(currentLength > closestLength) return -1;
		else return 0;
	}
	
	private void updateToolTip()
	{
		String text = "<html>";
		int size = highlightedRecords.size();
		for(int i = 0; i < size; i++)
			text += highlightedRecords.get(i) + (i != size - 1 ? "<br>" : "");
		text += "</html>";
		setToolTipText(text);
	}
	
	void updateValues()
	{
		firstRecordTime = getFirstRecordTime();
		lastRecordTime = getLastRecordTime();
		updateLowestAndHighestTemperature();
		lowestHumidity = showForecastError ? -100 : 0;
		highestHumidity = 100;
		firstVisibleTime = calcFirstVisibleTime();
		lastVisibleTime = calcLastVisbleTime();
		
		repaint();
	}
	
	private void updateLowestAndHighestTemperature()
	{
		if(showForecastError)
		{
			lowestTemperature = getLowestTemperatureFromForecastErrors();
			highestTemperature = getHighestTemperatureFromForecastErrors();
			int maxAbs = Math.max(Math.abs(lowestTemperature), Math.abs(highestTemperature));
			lowestTemperature = maxAbs * -1;
			highestTemperature = maxAbs;
		}
		else
		{
			lowestTemperature = getLowestTemperature();
			highestTemperature = getHighestTemperature();
		}
	}
	
	void updateData()
	{
		updateNewestRecords();
		if(showForecastError) updateForecastErrorData();
	}
	
	private void updateNewestRecords()
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
	
	private void updateForecastErrorData()
	{
		forecastErrorData.clear();
		
		createNewForecastErrorRecords(RecordsManager.getMeasureRecords(), RecordsManager.getForecastRecords(), false);
		createNewForecastErrorRecords(RecordsManager.getForecastRecords(), RecordsManager.getMeasureRecords(), true);
		
		Collections.sort(forecastErrorData);
	}
	
	private void createNewForecastErrorRecords(ArrayList<? extends Record> firstList, ArrayList<? extends Record> secondList, boolean invert)
	{
		for(Record record : firstList)
		{
			if(!isRecordProper(record)) continue;
			Record corresponding = getCorrespondingRecordFromList(record, secondList);
			if(corresponding == null) continue;
			int temperatureDiff = corresponding.getTemperature() - record.getTemperature();
			int humidityDiff = corresponding.getHumidity() - record.getHumidity();
			if(invert) temperatureDiff = 0 - temperatureDiff;
			if(invert) humidityDiff = 0 - humidityDiff;
			Record error = new ForecastErrorRecord(record.getTimeInSeconds(), temperatureDiff, humidityDiff);
			forecastErrorData.add(error);
		}
	}
	
	private Record getCorrespondingRecordFromList(Record record, ArrayList<? extends Record> list)
	{
		Record recordWithSameTime = findRecordWithSameTime(record, list);
		if(recordWithSameTime != null) return recordWithSameTime;
		
		Record earlier = null;
		Record later = null;
		for(Record next : list)
		{
			if(isRecordEarlierAndProper(next, record) && isRecordLater(next, earlier)) earlier = next;
			if(isRecordLaterAndProper(next, record) && isRecordEarlier(next, later)) later = next;
		}
		if(earlier == null || later == null) return null;
		float newTemperature = map(earlier.getTimeInSeconds(), later.getTimeInSeconds(),
								   earlier.getTemperature(), later.getTemperature(),
								   record.getTimeInSeconds());
		float newHumidity = map(earlier.getTimeInSeconds(), later.getTimeInSeconds(),
								earlier.getHumidity(), later.getHumidity(),
								record.getTimeInSeconds());
		return new Record(record.getTimeInSeconds(), Math.round(newTemperature), Math.round(newHumidity));
	}
	
	private Record findRecordWithSameTime(Record record, ArrayList<? extends Record> list)
	{
		for(Record next : list)
			if(isRecordProper(next) && next.getTimeInSeconds() == record.getTimeInSeconds())
				return next;
		return null;
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
	
	private int getLowestTemperatureFromForecastErrors()
	{
		Record lowest = getRecordWithLowestTemperaure(forecastErrorData);
		return lowest != null ? lowest.getTemperature() : 0;
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
	
	private int getHighestTemperatureFromForecastErrors()
	{
		Record highest = getRecordWithHighestTemperature(forecastErrorData);
		return highest != null ? highest.getTemperature() : 0;
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
		} //Te nawiasy klamrowe są bardzo potrzebne.
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
	
	int getTimeScaleRatio()
	{
		return Math.round((float) daysVisible / ((lastRecordTime - firstRecordTime) / SECONDS_IN_DAY) * 100);
	}
	
	void setShowMeasurement(boolean showMeasurement)
	{
		this.showMeasurement = showMeasurement;
	}
	
	void setShowForecast(boolean showForecast)
	{
		this.showForecast = showForecast;
	}
	
	void setShowForecastError(boolean showForecastError)
	{
		this.showForecastError = showForecastError;
	}
	
	void setShowTemperature(boolean showTemperature)
	{
		this.showTemperature = showTemperature;
	}
	
	void setShowHumidity(boolean showHumidity)
	{
		this.showHumidity = showHumidity;
	}
	
	void setDaysVisible(int daysVisible)
	{
		this.daysVisible = daysVisible;
	}
	
	void setOffsetPercent(int offsetPercent)
	{
		this.offsetPercent = offsetPercent;
	}
	
	void setCurrentSourceFilter(int currentSourceFilter)
	{
		this.currentSourceFilter = currentSourceFilter;
	}
	
	void setForecastCreationTimeFilter(int time)
	{
		this.forecastCreationTimeFilter = time;
	}
	
	void setForecastCreationTimeFilterNewest(boolean newest)
	{
		this.forecastCreationTimeFilterNewest = newest;
	}
}