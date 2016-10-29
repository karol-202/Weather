package pl.karol202.weather.ui.table;

import pl.karol202.weather.record.Record;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Date;

public class RecordsTableModel extends AbstractTableModel
{
	private String[] header;
	private ArrayList<Record> data;
	
	public RecordsTableModel(ArrayList<Record> data)
	{
		header = new String[] { "Czas", "Temperatura", "Wilgotność" };
		this.data = data;
	}
	
	@Override
	public int getRowCount()
	{
		return data.size();
	}
	
	@Override
	public int getColumnCount()
	{
		return header.length;
	}
	
	@Override
	public String getColumnName(int column)
	{
		return header[column];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return getValueAt(0, columnIndex).getClass();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Record record = data.get(rowIndex);
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
	
	protected ArrayList<Record> getData()
	{
		return data;
	}
}
