package pl.karol202.weather.ui.table.editor;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class IntegerCellEditor extends DefaultCellEditor
{
	private JFormattedTextField textField;
	
	public IntegerCellEditor(int min, int max)
	{
		super(new JFormattedTextField());
		textField = (JFormattedTextField) getComponent();
		textField.setHorizontalAlignment(SwingConstants.TRAILING);
		textField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		
		NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		formatter.setMinimum(min);
		formatter.setMaximum(max);
		textField.setFormatterFactory(new DefaultFormatterFactory(formatter));
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		JFormattedTextField textField =
				(JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
		textField.setValue(value);
		return textField;
	}
	
	@Override
	public Object getCellEditorValue()
	{
		Object value = textField.getValue();
		if(value instanceof Integer) return value;
		else if(value instanceof Number) return ((Number) value).intValue();
		return 0;
	}
	
	@Override
	public boolean stopCellEditing()
	{
		if(textField.isEditValid())
		{
			try { textField.commitEdit(); }
			catch(ParseException ignored) {}
			return super.stopCellEditing();
		}
		return false;
	}
}
