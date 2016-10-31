package pl.karol202.weather.ui.table.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public abstract class AdvancedCellRenderer extends JTextPane implements TableCellRenderer
{
	private Color unselectedBackground;
	
	public AdvancedCellRenderer()
	{
		unselectedBackground = getBackground();
		
		SimpleAttributeSet paragraphAttributes = new SimpleAttributeSet();
		StyleConstants.setAlignment(paragraphAttributes, StyleConstants.ALIGN_RIGHT);
		setParagraphAttributes(paragraphAttributes, false);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if(isSelected) setBackground(table.getSelectionBackground());
		else setBackground(unselectedBackground);
		
		return this;
	}
	
	protected void appendColoredText(String text, Color color)
	{
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, color);
		
		int length = getDocument().getLength();
		setCaretPosition(length);
		setCharacterAttributes(set, false);
		replaceSelection(text);
	}
}
