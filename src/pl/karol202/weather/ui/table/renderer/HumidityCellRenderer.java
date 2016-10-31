package pl.karol202.weather.ui.table.renderer;

import javax.swing.*;
import java.awt.*;

public class HumidityCellRenderer extends AdvancedCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		setText("");
		appendColoredText(value.toString(), !isSelected ? Color.BLACK : Color.WHITE);
		appendColoredText("%", !isSelected ? Color.GRAY : Color.WHITE);
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
