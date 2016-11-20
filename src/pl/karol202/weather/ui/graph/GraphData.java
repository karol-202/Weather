package pl.karol202.weather.ui.graph;

import pl.karol202.weather.record.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static pl.karol202.weather.ui.graph.Graph.map;

public class GraphData
{
	private final Graph graph;
	
	private int currentSourceFilter;
	private int forecastCreationTimeFilter;
	private boolean forecastCreationTimeFilterNewest;
	
	private HashMap<Integer, ForecastRecord> newestForecastRecords;
	private ArrayList<Record> highlightedRecords;
	private ArrayList<ForecastErrorRecord> forecastErrorData;
	
	private boolean interrupted;
	private ArrayList<MeasureRecord> copyOfMeasureRecords;
	private ArrayList<ForecastRecord> copyOfForecastRecords;
	
	public GraphData(Graph graph)
	{
		this.graph = graph;
		
		this.currentSourceFilter = -1;
		this.forecastCreationTimeFilter = 0;
		this.forecastCreationTimeFilterNewest = true;
		
		this.newestForecastRecords = new HashMap<>();
		this.highlightedRecords = new ArrayList<>();
		this.forecastErrorData = new ArrayList<>();
	}
	
	public void updateHighlightedRecords(int mouseTime)
	{
		highlightedRecords.clear();
		if(!graph.isShowingForecastError())
		{
			if(graph.isShowingMeasurement())
				for(Record record : RecordsManager.getMeasureRecords()) checkRecord(mouseTime, record);
			if(graph.isShowingForecast())
				for(Record record : RecordsManager.getForecastRecords()) checkRecord(mouseTime, record);
		}
		else
			for(Record record : forecastErrorData) checkRecord(mouseTime, record);
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
	
	public Record getFirstHighlightedRecord()
	{
		if(highlightedRecords.size() == 0) return null;
		else return highlightedRecords.get(0);
	}
	
	public void clearHighlightedRecords()
	{
		highlightedRecords.clear();
	}
	
	public String getToolTipText()
	{
		String text = "<html>";
		int size = highlightedRecords.size();
		for(int i = 0; i < size; i++)
			text += highlightedRecords.get(i) + (i != size - 1 ? "<br>" : "");
		text += "</html>";
		return text;
	}
	
	public void updateData()
	{
		this.copyOfMeasureRecords = new ArrayList<>(RecordsManager.getMeasureRecords());
		this.copyOfForecastRecords = new ArrayList<>(RecordsManager.getForecastRecords());
		updateNewestForecastRecords();
		updateForecastErrorData();
	}
	
	private void updateNewestForecastRecords()
	{
		newestForecastRecords.clear();
		for(ForecastRecord record : copyOfForecastRecords)
		{
			int time = record.getTimeInSeconds();
			if(newestForecastRecords.containsKey(time))
				if(record.getCreationTimeInSeconds() <= newestForecastRecords.get(time).getCreationTimeInSeconds()) continue;
			newestForecastRecords.put(time, record);
		}
	}
	
	private void updateForecastErrorData()
	{
		forecastErrorData.clear();
		
		createNewForecastErrorRecords(copyOfMeasureRecords, copyOfForecastRecords, false);
		if(interrupted) return;
		createNewForecastErrorRecords(copyOfForecastRecords, copyOfMeasureRecords, true);
		if(interrupted) return;
		
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
			
			if(interrupted = Thread.interrupted()) return;
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
	
	public ArrayList<ForecastErrorRecord> getForecastErrorData()
	{
		return forecastErrorData;
	}
	
	public int getFirstRecordTime()
	{
		Record first = null;
		if(graph.isShowingMeasurement() || graph.isShowingForecastError())
			first = getFirstRecordFromList(RecordsManager.getMeasureRecords());
		if(graph.isShowingForecast() || graph.isShowingForecastError())
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
	
	public int getLastRecordTime()
	{
		Record last = null;
		if(graph.isShowingMeasurement() || graph.isShowingForecastError())
			last = getLastRecordFromList(RecordsManager.getMeasureRecords());
		if(graph.isShowingForecast() || graph.isShowingForecastError())
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
	
	public int getLowestTemperature()
	{
		Record lowest = null;
		if(graph.isShowingMeasurement())
			lowest = getRecordWithLowestTemperaure(RecordsManager.getMeasureRecords());
		if(graph.isShowingForecast())
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
	
	public int getLowestTemperatureFromForecastErrors()
	{
		Record lowest = getRecordWithLowestTemperaure(forecastErrorData);
		return lowest != null ? (int) Math.floor(lowest.getTemperature()) : 0;
	}
	
	public int getHighestTemperature()
	{
		Record highest = null;
		if(graph.isShowingMeasurement())
			highest = getRecordWithHighestTemperature(RecordsManager.getMeasureRecords());
		if(graph.isShowingForecast())
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
	
	public int getHighestTemperatureFromForecastErrors()
	{
		Record highest = getRecordWithHighestTemperature(forecastErrorData);
		return highest != null ? (int) Math.ceil(highest.getTemperature()) : 0;
	}
	
	public boolean isRecordProper(Record record)
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
	
	public void setCurrentSourceFilter(int filter)
	{
		this.currentSourceFilter = filter;
	}
	
	public void setForecastCreationTimeFilter(int filter)
	{
		this.forecastCreationTimeFilter = filter;
	}
	
	public void setForecastCreationTimeFilterNewest(boolean filter)
	{
		this.forecastCreationTimeFilterNewest = filter;
	}
}