package pl.karol202.weather.ui.table.editor;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;

public class IntegerCellEditor extends DefaultCellEditor implements FocusListener
{
	private JFormattedTextField textField;
	
	public IntegerCellEditor(int min, int max)
	{
		super(new JFormattedTextField());
		textField = (JFormattedTextField) getComponent();
		textField.setHorizontalAlignment(SwingConstants.TRAILING);
		textField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		textField.addFocusListener(this);
		
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
		SwingUtilities.invokeLater(textField::selectAll);
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
		if(!textField.isEditValid()) return false;
		try { textField.commitEdit(); }
		catch(ParseException ignored) { }
		return super.stopCellEditing();
	}
	
	@Override
	public void focusGained(FocusEvent e)
	{
		SwingUtilities.invokeLater(textField::selectAll);
	}
	
	@Override
	public void focusLost(FocusEvent e) { }
}
