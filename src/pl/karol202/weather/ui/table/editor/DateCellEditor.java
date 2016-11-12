package pl.karol202.weather.ui.table.editor;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class DateCellEditor extends DefaultCellEditor
{
	private JFormattedTextField textField;
	private DateFormat format;
	
	public DateCellEditor()
	{
		super(new JFormattedTextField());
		textField = (JFormattedTextField) getComponent();
		textField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		
		format = DateFormat.getDateTimeInstance();
		DateFormatter formatter = new DateFormatter(format);
		textField.setFormatterFactory(new DefaultFormatterFactory(formatter));
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		if(!(value instanceof Date)) return null;
		Date date = (Date) value;
		textField.setText(format.format(date));
		return textField;
	}
	
	@Override
	public Object getCellEditorValue()
	{
		try { return format.parse(textField.getText()); }
		catch(ParseException e) { e.printStackTrace(); }
		return null;
	}
	
	@Override
	public boolean stopCellEditing()
	{
		if(!textField.isEditValid()) return false;
		try { textField.commitEdit(); }
		catch(ParseException ignored) {}
		return super.stopCellEditing();
	}
}
