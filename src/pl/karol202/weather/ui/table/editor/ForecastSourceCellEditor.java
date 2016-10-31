package pl.karol202.weather.ui.table.editor;

import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;
import java.awt.*;

public class ForecastSourceCellEditor extends DefaultCellEditor
{
	private JComboBox<String> comboBox;
	
	@SuppressWarnings("unchecked")
	public ForecastSourceCellEditor()
	{
		super(new JComboBox<String>());
		comboBox = (JComboBox<String>) getComponent();
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		comboBox.removeAllItems();
		RecordsManager.getForecastSources().forEach(comboBox::addItem);
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}
	
	@Override
	public Object getCellEditorValue()
	{
		return comboBox.getSelectedIndex();
	}
}