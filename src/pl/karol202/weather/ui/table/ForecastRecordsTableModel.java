package pl.karol202.weather.ui.table;

import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;

import java.util.ArrayList;
import java.util.Date;

public class ForecastRecordsTableModel extends RecordsTableModel
{
	public interface DataUpdateListener
	{
		void onDataUpdate();
	}
	
	private ArrayList<DataUpdateListener> listeners;
	
	public ForecastRecordsTableModel(ArrayList<Record> data)
	{
		super(data);
		this.listeners = new ArrayList<>();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(rowIndex >= getData().size()) return;
		Record record = getData().get(rowIndex);
		switch(columnIndex)
		{
		case 0:
			int timeInSeconds = (int) (((Date) aValue).getTime() / 1000);
			record.setTimeInSeconds(timeInSeconds);
			break;
		case 1:
			record.setTemperature((int) aValue);
			break;
		case 2:
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
