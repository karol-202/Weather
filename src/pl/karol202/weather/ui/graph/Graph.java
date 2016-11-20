package pl.karol202.weather.ui.graph;

import pl.karol202.weather.record.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

public class Graph extends JPanel
{
	private final int MARGIN = 30;
	private final int SECONDS_IN_DAY = 60 * 60 * 24;
	private final int MAX_HIGHLIGHT_DISTANCE = 20;
	private final int MAX_RECORDS_TIME_DIFFERENCE = 60 * 60 * 12;
	
	private boolean showMeasurementTemperature;
	private boolean showMeasurementHumidity;
	private boolean showMeasurementRain;
	private boolean showForecastTemperature;
	private boolean showForecastHumidity;
	private boolean showForecastRain;
	private boolean showForecastErrorTemperature;
	private boolean showForecastErrorHumidity;
	private int daysVisible;
	private int offsetPercent;
	
	private GraphData data;
	private DateFormat formatter;
	private int mouseX;
	private int firstRecordTime;
	private int lastRecordTime;
	private int lowestTemperature;
	private int highestTemperature;
	private int lowestHumidity;
	private int highestHumidity;
	private int firstVisibleTime;
	private int lastVisibleTime;
	
	public Graph()
	{
		this.showMeasurementTemperature = true;
		this.showMeasurementHumidity = true;
		this.showMeasurementRain = true;
		this.showForecastTemperature = true;
		this.showForecastHumidity = true;
		this.showForecastRain = true;
		this.showForecastErrorTemperature = false;
		this.showForecastErrorHumidity = false;
		this.daysVisible = 1;
		this.offsetPercent = 0;
		
		this.data = new GraphData(this);
		this.formatter = DateFormat.getDateTimeInstance();
		
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e)
			{
				Graph.this.onMouseExited();
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				Graph.this.onMouseMoved(e.getX());
			}
			
			@Override
			public void mouseDragged(MouseEvent e)
			{
				Graph.this.onMouseMoved(e.getX());
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
		drawData(graphics);
		drawHighlight(graphics);
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
		Record highlighted = data.getFirstHighlightedRecord();
		if(highlighted == null) return;
		int highlightedTime = highlighted.getTimeInSeconds();
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
			drawValueAsLine(g, RecordsManager.getMeasureRecords(),
							record -> mapYValue(lowestTemperature, highestTemperature, record.getTemperature()), Color.RED);
		if(showMeasurementHumidity)
			drawValueAsLine(g, RecordsManager.getMeasureRecords(),
							record -> mapYValue(lowestHumidity, highestHumidity, record.getHumidity()), Color.BLUE);
		if(showMeasurementRain)
			drawValueAsFill(g, RecordsManager.getMeasureRecords(),
							record -> map(0, 100, MARGIN, getHeight() - MARGIN, ((MeasureRecord) record).getRainLevel()),
							new Color(96, 192, 255, 192));
	}
	
	private void drawForecastRecords(Graphics2D g)
	{
		if(showForecastTemperature)
			drawValueAsLine(g, RecordsManager.getForecastRecords(),
							record -> mapYValue(lowestTemperature, highestTemperature, record.getTemperature()), Color.ORANGE);
		if(showForecastHumidity)
			drawValueAsLine(g, RecordsManager.getForecastRecords(),
							record -> mapYValue(lowestHumidity, highestHumidity, record.getHumidity()), Color.CYAN);
		if(showForecastRain)
			drawValueAsFill(g, RecordsManager.getForecastRecords(),
							record -> map(0, 100, MARGIN, getHeight() - MARGIN, ((ForecastRecord) record).getRainProbability()),
							new Color(96, 255, 160, 192));
	}
	
	private void drawErrorData(Graphics2D g)
	{
		if(showForecastErrorTemperature)
			drawValueAsLine(g, data.getForecastErrorData(),
					record -> mapYValue(lowestTemperature, highestTemperature, record.getTemperature()), Color.RED);
		if(showForecastErrorHumidity)
			drawValueAsLine(g, data.getForecastErrorData(),
					record -> mapYValue(lowestHumidity, highestHumidity, record.getHumidity()), Color.BLUE);
	}
	
	private void drawValueAsLine(Graphics2D g, ArrayList<? extends Record> records,
	                             Function<Record, Float> yFunction, Color color)
	{
		Record lastRecord = null;
		for(Record record : records)
		{
			if(!data.isRecordProper(record)) continue;
			if(lastRecord != null && record.getTimeInSeconds() - lastRecord.getTimeInSeconds() < MAX_RECORDS_TIME_DIFFERENCE)
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
	
	private void drawValueAsFill(Graphics2D g, ArrayList<? extends Record> records,
	                             Function<Record, Float> yFunction, Color color)
	{
		Record lastRecord = null;
		for(Record record : records)
		{
			if(!data.isRecordProper(record)) continue;
			if(lastRecord != null && record.getTimeInSeconds() - lastRecord.getTimeInSeconds() < MAX_RECORDS_TIME_DIFFERENCE)
			{
				int x = (int) map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, record.getTimeInSeconds());
				int y = Math.round(yFunction.apply(record));
				int lastX = (int) map(firstVisibleTime, lastVisibleTime, MARGIN, getWidth() - MARGIN, lastRecord.getTimeInSeconds());
				int lastY = Math.round(yFunction.apply(lastRecord));
				
				g.setColor(color);
				int[] xPoints = new int[] { lastX, x, x, lastX };
				int[] yPoints = new int[] { getHeight() - lastY, getHeight() - y, getHeight(), getHeight() };
				g.fillPolygon(xPoints, yPoints, 4);
			}
			lastRecord = record;
		}
	}
	
	public static float map(float srcMin, float srcMax, float dstMin, float dstMax, float value)
	{
		return (value - srcMin) / (srcMax - srcMin) * (dstMax - dstMin) + dstMin;
	}
	
	private float mapYValue(float yMin, float yMax, float value)
	{
		return map(yMin, yMax, getHeight() - MARGIN, MARGIN, value);
	}
	
	private void onMouseExited()
	{
		data.clearHighlightedRecords();
		repaint();
	}
	
	private void onMouseMoved(int x)
	{
		mouseX = x;
		int time = (int) map(MARGIN, getWidth() - MARGIN, firstVisibleTime, lastVisibleTime, x);
		data.updateHighlightedRecords(time);
		repaint();
		updateToolTip();
	}
	
	private void updateToolTip()
	{
		setToolTipText(data.getToolTipText());
	}
	
	public void updateValues()
	{
		firstRecordTime = data.getFirstRecordTime();
		lastRecordTime = data.getLastRecordTime();
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
			lowestTemperature = data.getLowestTemperatureFromForecastErrors();
			highestTemperature = data.getHighestTemperatureFromForecastErrors();
			int maxAbs = Math.max(Math.abs(lowestTemperature), Math.abs(highestTemperature));
			lowestTemperature = maxAbs * -1;
			highestTemperature = maxAbs;
		}
		else
		{
			lowestTemperature = data.getLowestTemperature();
			highestTemperature = data.getHighestTemperature();
		}
	}
	
	public void updateData()
	{
		data.updateData();
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
	
	public boolean isShowingMeasurement()
	{
		return showMeasurementTemperature || showMeasurementHumidity || showMeasurementRain;
	}
	
	public boolean isShowingForecast()
	{
		return showForecastTemperature || showForecastHumidity || showForecastRain;
	}
	
	public boolean isShowingForecastError()
	{
		return showForecastErrorTemperature || showForecastErrorHumidity;
	}
	
	public void setShowMeasurementTemperature(boolean show)
	{
		this.showMeasurementTemperature = show;
	}
	
	public void setShowMeasurementHumidity(boolean show)
	{
		this.showMeasurementHumidity = show;
	}
	
	public void setShowMeasurementRain(boolean show)
	{
		this.showMeasurementRain = show;
	}
	
	public void setShowForecastTemperature(boolean show)
	{
		this.showForecastTemperature = show;
	}
	
	public void setShowForecastHumidity(boolean show)
	{
		this.showForecastHumidity = show;
	}
	
	public void setShowForecastRain(boolean show)
	{
		this.showForecastRain = show;
	}
	
	public void setShowForecastErrorTemperature(boolean show)
	{
		this.showForecastErrorTemperature = show;
	}
	
	public void setShowForecastErrorHumidity(boolean show)
	{
		this.showForecastErrorHumidity = show;
	}
	
	public void setDaysVisible(int daysVisible)
	{
		this.daysVisible = daysVisible;
	}
	
	public void setOffsetPercent(int offsetPercent)
	{
		this.offsetPercent = offsetPercent;
	}
	
	public void setCurrentSourceFilter(int filter)
	{
		data.setCurrentSourceFilter(filter);
	}
	
	public void setForecastCreationTimeFilter(int filter)
	{
		data.setForecastCreationTimeFilter(filter);
	}
	
	public void setForecastCreationTimeFilterNewest(boolean filter)
	{
		data.setForecastCreationTimeFilterNewest(filter);
	}
}