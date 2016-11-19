package pl.karol202.weather.ui;

import pl.karol202.weather.record.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

public class GraphPanel extends JPanel
{
	private final int MARGIN = 30;
	private final int SECONDS_IN_DAY = 60 * 60 * 24;
	private final int MAX_HIGHLIGHT_DISTANCE = 20;
	
	private boolean showMeasurementTemperature;
	private boolean showMeasurementHumidity;
	private boolean showForecastTemperature;
	private boolean showForecastHumidity;
	private boolean showForecastErrorTemperature;
	private boolean showForecastErrorHumidity;
	private int daysVisible;
	private int offsetPercent;
	private int currentSourceFilter;
	private int forecastCreationTimeFilter;
	private boolean forecastCreationTimeFilterNewest;
	
	private DateFormat formatter;
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
	private ArrayList<ForecastErrorRecord> forecastErrorData;
	
	public GraphPanel()
	{
		this.formatter = DateFormat.getDateTimeInstance();
		this.showMeasurementTemperature = true;
		this.showMeasurementHumidity = true;
		this.showForecastTemperature = true;
		this.showForecastHumidity = true;
		this.showForecastErrorTemperature = false;
		this.showForecastErrorHumidity = false;
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
		if(!isShowingForecastError())
			g.drawLine(MARGIN, getHeight() - MARGIN, getWidth() - MARGIN, getHeight() - MARGIN); //Bottom side
		else
			g.drawLine(MARGIN, getHeight() / 2, getWidth() - MARGIN, getHeight() / 2); //Center line
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
		if(showForecastErrorTemperature) g.drawString("0°C", MARGIN - 18, (getHeight() / 2) + 2);
	}
	
	private void drawTextsHumidity(Graphics2D g)
	{
		g.setColor(Color.BLUE);
		if(showForecastErrorHumidity)
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
		if(RecordsManager.getMeasureRecords() != null) drawMeasurementRecords(g);
		if(RecordsManager.getForecastRecords() != null) drawForecastRecords(g);
		drawErrorData(g);
	}
	
	private void drawMeasurementRecords(Graphics2D g)
	{
		if(showMeasurementTemperature)
			drawValueFromList(g, RecordsManager.getMeasureRecords(),
							  record -> mapYValue(lowestTemperature, highestTemperature, record.getTemperature()), Color.RED);
		if(showMeasurementHumidity)
			drawValueFromList(g, RecordsManager.getMeasureRecords(),
							  record -> mapYValue(lowestHumidity, highestHumidity, record.getHumidity()), Color.BLUE);
	}
	
	private void drawForecastRecords(Graphics2D g)
	{
		if(showForecastTemperature)
			drawValueFromList(g, RecordsManager.getForecastRecords(),
					record -> mapYValue(lowestTemperature, highestTemperature, record.getTemperature()), Color.ORANGE);
		if(showForecastHumidity)
			drawValueFromList(g, RecordsManager.getForecastRecords(),
					record -> mapYValue(lowestHumidity, highestHumidity, record.getHumidity()), Color.CYAN);
	}
	
	private void drawErrorData(Graphics2D g)
	{
		if(showForecastErrorTemperature)
			drawValueFromList(g, forecastErrorData,
					record -> mapYValue(lowestTemperature, highestTemperature, record.getTemperature()), Color.RED);
		if(showForecastErrorHumidity)
			drawValueFromList(g, forecastErrorData,
					record -> mapYValue(lowestHumidity, highestHumidity, record.getHumidity()), Color.BLUE);
	}
	
	private void drawValueFromList(Graphics2D g, ArrayList<? extends Record> records,
	                               Function<Record, Float> yFunction, Color color)
	{
		Record lastRecord = null;
		for(Record record : records)
		{
			if(!isRecordProper(record)) continue;
			if(lastRecord != null)
			{
				int x = (int) map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, record.getTimeInSeconds());
				int y = Math.round(yFunction.apply(record));
				int lastX = (int) map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, lastRecord.getTimeInSeconds());
				int lastY = Math.round(yFunction.apply(lastRecord));
				g.setColor(color);
				g.drawLine(lastX, lastY, x, y);
			}
			lastRecord = record;
		}
	}
	
	private float map(float srcMin, float srcMax, float dstMin, float dstMax, float value)
	{
		return (value - srcMin) / (srcMax - srcMin) * (dstMax - dstMin) + dstMin;
	}
	
	private float mapYValue(float yMin, float yMax, float value)
	{
		return map(yMin, yMax, getHeight() - MARGIN, MARGIN, value);
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
		if(!isShowingForecastError())
		{
			if(isShowingMeasurement())
				for(Record record : RecordsManager.getMeasureRecords()) checkRecord(mouseTime, record);
			if(isShowingForecast())
				for(Record record : RecordsManager.getForecastRecords()) checkRecord(mouseTime, record);
		}
		else
			for(Record record : forecastErrorData) checkRecord(mouseTime, record);
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
		lowestHumidity = isShowingForecastError() ? -100 : 0;
		highestHumidity = 100;
		firstVisibleTime = calcFirstVisibleTime();
		lastVisibleTime = calcLastVisbleTime();
		
		repaint();
	}
	
	private void updateLowestAndHighestTemperature()
	{
		if(isShowingForecastError())
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
		updateNewestForecastRecords();
		updateForecastErrorData();
	}
	
	private void updateNewestForecastRecords()
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
			float temperatureDiff = corresponding.getTemperature() - record.getTemperature();
			float humidityDiff = corresponding.getHumidity() - record.getHumidity();
			if(invert) temperatureDiff = 0 - temperatureDiff;
			if(invert) humidityDiff = 0 - humidityDiff;
			ForecastErrorRecord error = new ForecastErrorRecord(record.getTimeInSeconds(), temperatureDiff, humidityDiff);
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
		return new MeasureRecord(record.getTimeInSeconds(), Math.round(newTemperature), Math.round(newHumidity), 0);
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
		if(isShowingMeasurement() || isShowingForecastError())
			first = getFirstRecordFromList(RecordsManager.getMeasureRecords());
		if(isShowingForecast() || isShowingForecastError())
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
		if(isShowingMeasurement() || isShowingForecastError())
			last = getLastRecordFromList(RecordsManager.getMeasureRecords());
		if(isShowingForecast() || isShowingForecastError())
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
		if(isShowingMeasurement())
			lowest = getRecordWithLowestTemperaure(RecordsManager.getMeasureRecords());
		if(isShowingForecast())
		{
			Record lowestFromForecastRecords = getRecordWithLowestTemperaure(RecordsManager.getForecastRecords());
			if(hasRecordLowerTemperature(lowestFromForecastRecords, lowest))
				lowest = lowestFromForecastRecords;
		}
		return lowest != null ? (int) Math.floor(lowest.getTemperature()) : 0;
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
		return lowest != null ? (int) Math.floor(lowest.getTemperature()) : 0;
	}
	
	private int getHighestTemperature()
	{
		Record highest = null;
		if(isShowingMeasurement())
			highest = getRecordWithHighestTemperature(RecordsManager.getMeasureRecords());
		if(isShowingForecast())
		{
			Record highestFromForecastRecords = getRecordWithHighestTemperature(RecordsManager.getForecastRecords());
			if(hasRecordHigherTemperature(highestFromForecastRecords, highest))
				highest = highestFromForecastRecords;
		}
		return highest != null ? (int) Math.ceil(highest.getTemperature()) : 1;
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
		return highest != null ? (int) Math.ceil(highest.getTemperature()) : 0;
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
	
	private boolean isShowingMeasurement()
	{
		return showMeasurementTemperature || showMeasurementHumidity;
	}
	
	private boolean isShowingForecast()
	{
		return showForecastTemperature || showForecastHumidity;
	}
	
	private boolean isShowingForecastError()
	{
		return showForecastErrorTemperature || showForecastErrorHumidity;
	}
	
	void setShowMeasurementTemperature(boolean show)
	{
		this.showMeasurementTemperature = show;
	}
	
	void setShowMeasurementHumidity(boolean show)
	{
		this.showMeasurementHumidity = show;
	}
	
	void setShowForecastTemperature(boolean show)
	{
		this.showForecastTemperature = show;
	}
	
	void setShowForecastHumidity(boolean show)
	{
		this.showForecastHumidity = show;
	}
	
	void setShowForecastErrorTemperature(boolean show)
	{
		this.showForecastErrorTemperature = show;
	}
	
	void setShowForecastErrorHumidity(boolean show)
	{
		this.showForecastErrorHumidity = show;
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