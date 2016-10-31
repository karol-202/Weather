package pl.karol202.weather.ui.table.model;

import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;

import java.util.Date;

public class MeasureRecordsTableModel extends RecordsTableModel<Record>
{
	private static final String[] header = new String[] { "Czas", "Temperatura", "Wilgotność" };
	
	public MeasureRecordsTableModel()
	{
		super(header, RecordsManager.getMeasureRecords());
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Record record = getData().get(rowIndex);
		switch(columnIndex)
		{
		case 0:
			long timeInMillis = ((long) record.getTimeInSeconds()) * 1000;
			return new Date(timeInMillis);
		case 1:
			return record.getTemperature();
		case 2:
			return record.getHumidity();
		}
		return null;
	}
}
