package pl.karol202.weather.ui.table;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public abstract class RecordsTableModel<R> extends AbstractTableModel
{
	private String[] header;
	private ArrayList<R> data;
	
	public RecordsTableModel(String[] header, ArrayList<R> data)
	{
		this.header = header;
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
	
	protected ArrayList<R> getData()
	{
		return data;
	}
}
