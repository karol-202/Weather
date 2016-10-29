package pl.karol202.weather.ui.table;

import pl.karol202.weather.record.Record;

import java.util.ArrayList;
import java.util.Date;

public class MeasureRecordsTableModel extends RecordsTableModel<Record>
{
	public MeasureRecordsTableModel(ArrayList<Record> data)
	{
		super(new String[] { "Czas", "Temperatura", "Wilgotność" }, data);
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
