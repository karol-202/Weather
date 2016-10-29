package pl.karol202.weather.ui.table;

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
	
	private ArrayList<DataUpdateListener> listeners;
	
	public ForecastRecordsTableModel(ArrayList<ForecastRecord> data)
	{
		super(new String[] { "Czas", "Czas utworzenia rekordu", "Temperatura", "Wilgotność" }, data);
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
			return record.getTemperature();
		case 3:
			return record.getHumidity();
		}
		return null;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(rowIndex >= getData().size()) return;
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
			record.setTemperature((int) aValue);
			break;
		case 3:
			record.setHumidity((int) aValue);
			break;
		}
		fireTableCellUpdated(rowIndex, columnIndex);
		RecordsManager.save();
		listeners.forEach(DataUpdateListener::onDataUpdate);
	}
	
	public void addListener(DataUpdateListener listener)
	{
		listeners.add(listener);
	}
}
