package pl.karol202.weather;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.DateFormat;

public class RecordsTableRenderer extends DefaultTableCellRenderer
{
	private DateFormat formatter;
	
	public RecordsTableRenderer()
	{
		super();
	}
	
	public void setValue(Object value)
	{
		if (formatter == null) formatter = DateFormat.getDateTimeInstance();
		setText((value == null) ? "" : formatter.format(value));
	}
}
