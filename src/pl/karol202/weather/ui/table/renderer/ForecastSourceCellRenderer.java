package pl.karol202.weather.ui.table.renderer;

import pl.karol202.weather.record.RecordsManager;

import javax.swing.table.DefaultTableCellRenderer;

public class ForecastSourceCellRenderer extends DefaultTableCellRenderer
{
	@Override
	protected void setValue(Object value)
	{
		int source = (int) value;
		boolean existing = source != -1 && source < RecordsManager.getForecastSources().size();
		setText(existing ? RecordsManager.getForecastSources().get(source) : "");
	}
}