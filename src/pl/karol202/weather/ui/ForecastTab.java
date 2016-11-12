package pl.karol202.weather.ui;

import pl.karol202.weather.record.ForecastRecord;
import pl.karol202.weather.record.RecordsManager;
import pl.karol202.weather.ui.table.editor.DateCellEditor;
import pl.karol202.weather.ui.table.editor.ForecastSourceCellEditor;
import pl.karol202.weather.ui.table.editor.IntegerCellEditor;
import pl.karol202.weather.ui.table.model.ForecastRecordsTableModel;
import pl.karol202.weather.ui.table.renderer.DateCellRenderer;
import pl.karol202.weather.ui.table.renderer.ForecastSourceCellRenderer;
import pl.karol202.weather.ui.table.renderer.HumidityCellRenderer;
import pl.karol202.weather.ui.table.renderer.TemperatureCellRenderer;

import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Iterator;

import static javax.swing.JOptionPane.*;

public class ForecastTab
{
	private class KeyListener implements java.awt.event.KeyListener
	{
		@Override
		public void keyTyped(KeyEvent e) { }
		
		@Override
		public void keyPressed(KeyEvent e) { }
		
		@Override
		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() != KeyEvent.VK_DELETE) return;
			
			int deleted = 0;
			for(int row : tableForecast.getSelectedRows())
			{
				RecordsManager.getForecastRecords().remove(row - deleted);
				deleted++;
			}
			RecordsManager.save();
			forecastTableModel.fireTableDataChanged();
			parent.updateGraph();
		}
	}
	
	private FormMain parent;
	private ForecastRecordsTableModel forecastTableModel;
	private int currentSourceFilter;
	
	private JTable tableForecast;
	private JButton buttonAddRecord;
	private JComboBox<String> comboBoxSources;
	private JButton buttonAddSource;
	private JButton buttonEditSource;
	private JButton buttonDeleteSource;
	
	ForecastTab(FormMain parent)
	{
		this.parent = parent;
	}
	
	void init()
	{
		forecastTableModel = new ForecastRecordsTableModel();
		forecastTableModel.addListener(() -> parent.updateGraph());
		
		buttonAddRecord.addActionListener(e -> {
			int now = (int) (new Date().getTime() / 1000);
			RecordsManager.getForecastRecords().add(new ForecastRecord(now, now, -1, 0, 0));
			RecordsManager.save();
			forecastTableModel.fireTableDataChanged();
			parent.updateGraph();
		});
		
		tableForecast.setModel(forecastTableModel);
		tableForecast.setRowSelectionAllowed(true);
		tableForecast.setColumnSelectionAllowed(false);
		tableForecast.getTableHeader().setReorderingAllowed(false);
		tableForecast.setDefaultRenderer(Date.class, new DateCellRenderer());
		tableForecast.setDefaultEditor(Date.class, new DateCellEditor());
		TableColumn sourceColumn = tableForecast.getColumnModel().getColumn(2);
		sourceColumn.setMaxWidth(100);
		sourceColumn.setCellEditor(new ForecastSourceCellEditor());
		sourceColumn.setCellRenderer(new ForecastSourceCellRenderer());
		TableColumn temperatureColumn = tableForecast.getColumnModel().getColumn(3);
		temperatureColumn.setMaxWidth(100);
		temperatureColumn.setCellEditor(new IntegerCellEditor(-128, 127));
		temperatureColumn.setCellRenderer(new TemperatureCellRenderer());
		TableColumn humidityColumn = tableForecast.getColumnModel().getColumn(4);
		humidityColumn.setMaxWidth(80);
		humidityColumn.setCellEditor(new IntegerCellEditor(0, 100));
		humidityColumn.setCellRenderer(new HumidityCellRenderer());
		tableForecast.addKeyListener(new KeyListener());
		
		comboBoxSources.addItemListener(e -> updateSourceFilter());
		updateSources();
		
		buttonAddSource.addActionListener(e -> showSourceDialog(true));
		buttonEditSource.addActionListener(e -> showSourceDialog(false));
		buttonDeleteSource.addActionListener(e -> showSourceDeleteDialog());
	}
	
	private void updateSources()
	{
		updateSourcesComboBox();
		updateSourceFilter();
		parent.updateSources();
	}
	
	private void updateSourcesComboBox()
	{
		String[] sources = new String[RecordsManager.getForecastSources().size()];
		RecordsManager.getForecastSources().toArray(sources);
		
		String[] names = new String[sources.length + 1];
		names[0] = "  Brak filtra";
		System.arraycopy(sources, 0, names, 1, sources.length);
		comboBoxSources.setModel(new DefaultComboBoxModel<>(names));
	}
	
	private void updateSourceFilter()
	{
		int newSourceFilter = comboBoxSources.getSelectedIndex() - 1;
		if(newSourceFilter == -1 && currentSourceFilter != -1)
		{
			buttonEditSource.setEnabled(false);
			buttonDeleteSource.setEnabled(false);
		}
		else if(newSourceFilter != -1 && currentSourceFilter == -1)
		{
			buttonEditSource.setEnabled(true);
			buttonDeleteSource.setEnabled(true);
		}
		currentSourceFilter = newSourceFilter;
		
		forecastTableModel.setCurrentSourceFilter(currentSourceFilter);
		forecastTableModel.fireTableDataChanged();
	}
	
	private enum SourceAddDialogOptions
	{
		OK("OK", OK_OPTION),
		CANCEL("Anuluj", CANCEL_OPTION);
		
		private String name;
		private int value;
		
		SourceAddDialogOptions(String name, int value)
		{
			this.name = name;
			this.value = value;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getValue()
		{
			return value;
		}
		
		public static String[] getNames()
		{
			String[] names = new String[values().length];
			for(int i = 0; i < values().length; i++) names[i] = values()[i].getName();
			return names;
		}
		
		private static SourceAddDialogOptions findOptionByName(String option)
		{
			for(SourceAddDialogOptions o : values())
				if(option.equals(o.getName())) return o;
			return null;
		}
	}
	
	private void showSourceDialog(boolean newSource)
	{
		String message = newSource ? "Podaj nazwę nowego źródła prognozy pogody." : "Zmień nazwę źródła prognozy pogody.";
		JOptionPane pane = new JOptionPane(message,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				SourceAddDialogOptions.getNames(),
				SourceAddDialogOptions.values()[0].getName());
		pane.setWantsInput(true);
		if(!newSource)
		{
			checkCurrentSourceFilter();
			pane.setInitialSelectionValue(RecordsManager.getForecastSources().get(currentSourceFilter));
		}
		JDialog dialog = pane.createDialog(newSource ? "Nowe źródło" : "Edytuj źródło");
		dialog.setVisible(true);
		dialog.dispose();
		
		if(pane.getValue() == null) return;
		String optionName = pane.getValue().toString();
		SourceAddDialogOptions option = SourceAddDialogOptions.findOptionByName(optionName);
		if(option == null) throw new RuntimeException("Error: Invalid dialog option: " + optionName);
		int optionValue = option.getValue();
		
		if(optionValue == OK_OPTION)
		{
			if(newSource) addSource(pane.getInputValue().toString());
			else editCurrentSource(pane.getInputValue().toString());
		}
	}
	
	private void showSourceDeleteDialog()
	{
		String[] options = new String[] { "Usuń", "Anuluj" };
		int option = JOptionPane.showOptionDialog(parent,
				"Czy na pewno chcesz usunąć to źródło? " +
						"Wszystkie rekordy z tego źródła zostaną usunięte.",
				"Usuń źródło",
				YES_NO_OPTION,
				QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		if(option == YES_OPTION) deleteSourceAndRecordsUsingIt();
	}
	
	private void addSource(String name)
	{
		RecordsManager.getForecastSources().add(name);
		RecordsManager.save();
		updateSources();
	}
	
	private void editCurrentSource(String newName)
	{
		checkCurrentSourceFilter();
		RecordsManager.getForecastSources().set(currentSourceFilter, newName);
		RecordsManager.save();
		updateSources();
	}
	
	private void deleteSourceAndRecordsUsingIt()
	{
		checkCurrentSourceFilter();
		
		Iterator<ForecastRecord> iterator = RecordsManager.getForecastRecords().iterator();
		while(iterator.hasNext())
		{
			ForecastRecord record = iterator.next();
			if(record.getForecastSource() == currentSourceFilter) iterator.remove();
			else if(record.getForecastSource() > currentSourceFilter)
				record.setForecastSource(record.getForecastSource() - 1);
		}
		
		RecordsManager.getForecastSources().remove(currentSourceFilter);
		RecordsManager.save();
		updateSources();
		parent.updateGraph();
	}
	
	private void checkCurrentSourceFilter()
	{
		if(currentSourceFilter == -1 || currentSourceFilter >= RecordsManager.getForecastSources().size())
			throw new RuntimeException("Cannot delete this filter.");
	}
	
	void setTableForecast(JTable tableForecast)
	{
		this.tableForecast = tableForecast;
	}
	
	void setButtonAddRecord(JButton buttonAddRecord)
	{
		this.buttonAddRecord = buttonAddRecord;
	}
	
	void setComboBoxSources(JComboBox<String> comboBoxSources)
	{
		this.comboBoxSources = comboBoxSources;
	}
	
	void setButtonAddSource(JButton buttonAddSource)
	{
		this.buttonAddSource = buttonAddSource;
	}
	
	void setButtonEditSource(JButton buttonEditSource)
	{
		this.buttonEditSource = buttonEditSource;
	}
	
	void setButtonDeleteSource(JButton buttonDeleteSource)
	{
		this.buttonDeleteSource = buttonDeleteSource;
	}
}