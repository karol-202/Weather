package pl.karol202.weather.ui.table.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class UnitCellRenderer extends JTextPane implements TableCellRenderer
{
	private Color unselectedBackground;
	private String unit;
	
	public UnitCellRenderer(String unit)
	{
		this.unselectedBackground = getBackground();
		this.unit = unit;
		
		SimpleAttributeSet paragraphAttributes = new SimpleAttributeSet();
		StyleConstants.setAlignment(paragraphAttributes, StyleConstants.ALIGN_RIGHT);
		setParagraphAttributes(paragraphAttributes, false);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if(isSelected) setBackground(table.getSelectionBackground());
		else setBackground(unselectedBackground);
		
		setText("");
		appendColoredText(value.toString(), !isSelected ? Color.BLACK : Color.WHITE);
		appendColoredText(unit, !isSelected ? Color.GRAY : Color.WHITE);
		
		return this;
	}
	
	private void appendColoredText(String text, Color color)
	{
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, color);
		
		int length = getDocument().getLength();
		setCaretPosition(length);
		setCharacterAttributes(set, false);
		replaceSelection(text);
	}
}
