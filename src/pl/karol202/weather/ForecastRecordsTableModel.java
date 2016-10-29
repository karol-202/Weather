package pl.karol202.weather;

import java.util.ArrayList;
import java.util.Date;

public class ForecastRecordsTableModel extends RecordsTableModel
{
	public ForecastRecordsTableModel(ArrayList<Record> data)
	{
		super(data);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
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
	}
}
