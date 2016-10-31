package pl.karol202.weather.ui.table.model;

import pl.karol202.weather.record.ForecastRecord;
import pl.karol202.weather.record.RecordsManager;

import java.util.ArrayList;
import java.util.Date;

public class ForecastRecordsTableModel extends RecordsTableModel<ForecastRecord>
{
	public interface DataUpdateListener
	{
		void onDataUpdate();
	}
	
	private static final String[] header = new String[] { "Czas",
														  "Czas utworzenia rekordu",
														  "Źródło",
														  "Temperatura",
														  "Wilgotność" };
	
	private ArrayList<DataUpdateListener> listeners;
	private int currentSourceFilter;
	
	public ForecastRecordsTableModel()
	{
		super(header, new ArrayList<>());
		this.listeners = new ArrayList<>();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ForecastRecord record = getData().get(rowIndex);
		switch(columnIndex)
		{
		case 0:
			long timeInMillis = ((long) record.getTimeInSeconds()) * 1000;
			return new Date(timeInMillis);
		case 1:
			long creationTimeInMillis = ((long) record.getCreationTimeInSeconds()) * 1000;
			return new Date(creationTimeInMillis);
		case 2:
			return record.getForecastSource();
		case 3:
			return record.getTemperature();
		case 4:
			return record.getHumidity();
		}
		return null;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(rowIndex < 0 || rowIndex >= getData().size()) return;
		ForecastRecord record = getData().get(rowIndex);
		switch(columnIndex)
		{
		case 0:
			int timeInSeconds = (int) (((Date) aValue).getTime() / 1000);
			record.setTimeInSeconds(timeInSeconds);
			break;
		case 1:
			int creationTimeInSeconds = (int) (((Date) aValue).getTime() / 1000);
			record.setCreationTimeInSeconds(creationTimeInSeconds);
			break;
		case 2:
			record.setForecastSource((int) aValue);
			break;
		case 3:
			record.setTemperature((int) aValue);
			break;
		case 4:
			record.setHumidity((int) aValue);
			break;
		}
		fireTableCellUpdated(rowIndex, columnIndex);
		RecordsManager.save();
		listeners.forEach(DataUpdateListener::onDataUpdate);
	}
	
	@Override
	public void fireTableDataChanged()
	{
		updateRecords();
		super.fireTableDataChanged();
	}
	
	private void updateRecords()
	{
		getData().clear();
		if(currentSourceFilter == -1) RecordsManager.getForecastRecords().forEach(getData()::add);
		else RecordsManager.getForecastRecords()
						   .stream()
						   .filter(record -> record.getForecastSource() == currentSourceFilter)
						   .forEach(getData()::add);
	}
	
	public void addListener(DataUpdateListener listener)
	{
		listeners.add(listener);
	}
	
	public void setCurrentSourceFilter(int currentSourceFilter)
	{
		this.currentSourceFilter = currentSourceFilter;
	}
}