package pl.karol202.weather.ui.table;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.DateFormat;

public class RecordsTableRenderer extends DefaultTableCellRenderer
{
	private DateFormat formatter;
	
	public RecordsTableRenderer()
	{
		super();
		this.formatter = DateFormat.getDateTimeInstance();
	}
	
	public void setValue(Object value)
	{
		setText((value == null) ? "" : formatter.format(value));
	}
}
