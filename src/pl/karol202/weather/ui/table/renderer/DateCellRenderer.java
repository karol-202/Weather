package pl.karol202.weather.ui.table.renderer;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.DateFormat;

public class DateCellRenderer extends DefaultTableCellRenderer
{
	private DateFormat formatter;
	
	public DateCellRenderer()
	{
		super();
		this.formatter = DateFormat.getDateTimeInstance();
	}
	
	public void setValue(Object value)
	{
		setText((value == null) ? "" : formatter.format(value));
	}
}
