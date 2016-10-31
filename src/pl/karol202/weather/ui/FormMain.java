package pl.karol202.weather.ui;

import pl.karol202.weather.hardware.Connector;
import pl.karol202.weather.hardware.Connector.ConnectionListener;
import pl.karol202.weather.record.ForecastRecord;
import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;
import pl.karol202.weather.ui.table.editor.DateCellEditor;
import pl.karol202.weather.ui.table.editor.ForecastSourceCellEditor;
import pl.karol202.weather.ui.table.editor.IntegerCellEditor;
import pl.karol202.weather.ui.table.model.ForecastRecordsTableModel;
import pl.karol202.weather.ui.table.model.MeasureRecordsTableModel;
import pl.karol202.weather.ui.table.renderer.DateCellRenderer;
import pl.karol202.weather.ui.table.renderer.ForecastSourceCellRenderer;
import pl.karol202.weather.ui.table.renderer.HumidityCellRenderer;
import pl.karol202.weather.ui.table.renderer.TemperatureCellRenderer;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import static javax.swing.JOptionPane.*;

public class FormMain extends JFrame implements ConnectionListener
{
	private Connector connector;
	private MeasureRecordsTableModel measureTableModel;
	private ForecastRecordsTableModel forecastTableModel;
	private SpinnerNumberModel spinnerModelNumber;
	private int currentSourceFilter;
	
	private JPanel root;
	private JTable tableMeasurement;
	private JButton buttonConnect;
	private JButton buttonSetTime;
	private JButton buttonSaveTime;
	private JButton buttonGetData;
	private JButton buttonReset;
	private JComboBox<String> comboBoxPort;
	private JButton buttonRefresh;
	private JProgressBar progressBarMemory;
	
	private JButton buttonAddRecord;
	private JTable tableForecast;
	private JComboBox<String> comboBoxSources;
	private JButton buttonAddSource;
	private JButton buttonEditSource;
	private JButton buttonDeleteSource;
	
	private JCheckBox checkBoxMeasure;
	private JCheckBox checkBoxForecast;
	private JCheckBox checkBoxTemperature;
	private JCheckBox checkBoxHumidity;
	private JScrollBar scrollBarOffset;
	private JSpinner spinnerScale;
	private GraphPanel graph;
	
	public FormMain()
	{
		super("Weather");
		setContentPane(root);
		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocation(500, 200);
		setResizable(false);
		setVisible(true);
		
		RecordsManager.init();
		initPorts();
		
		initTabMeasure();
		initTabForecast();
		initTabGraph();
	}
	
	private void initPorts()
	{
		Connector.refreshPorts();
		if(connector != null) connector.checkConnection();
		updatePortsComboBox();
	}
	
	private void initTabMeasure()
	{
		measureTableModel = new MeasureRecordsTableModel(RecordsManager.getMeasureRecords());
		
		buttonConnect.addActionListener(e -> onConnectClick());
		buttonSetTime.addActionListener(e -> onSetTimeClick());
		buttonSaveTime.addActionListener(e -> onSaveTimeClick());
		buttonGetData.addActionListener(e -> onGetDataClick());
		buttonReset.addActionListener(e -> onResetClick());
		buttonRefresh.addActionListener(e -> onRefreshClick());
		
		checkBoxMeasure.addActionListener(e -> updateGraph());
		checkBoxForecast.addActionListener(e -> updateGraph());
		checkBoxTemperature.addActionListener(e -> updateGraph());
		checkBoxHumidity.addActionListener(e -> updateGraph());
		
		tableMeasurement.setModel(measureTableModel);
		tableMeasurement.setRowSelectionAllowed(true);
		tableMeasurement.setColumnSelectionAllowed(false);
		tableMeasurement.getTableHeader().setReorderingAllowed(false);
		tableMeasurement.setDefaultRenderer(Date.class, new DateCellRenderer());
		tableMeasurement.getColumnModel().getColumn(1).setMaxWidth(100);
		tableMeasurement.getColumnModel().getColumn(1).setCellRenderer(new TemperatureCellRenderer());
		tableMeasurement.getColumnModel().getColumn(2).setMaxWidth(80);
		tableMeasurement.getColumnModel().getColumn(2).setCellRenderer(new HumidityCellRenderer());
		tableMeasurement.addKeyListener(new KeyListener()
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
				for(int row : tableMeasurement.getSelectedRows())
				{
					RecordsManager.getMeasureRecords().remove(row - deleted);
					deleted++;
				}
				RecordsManager.save();
				measureTableModel.fireTableDataChanged();
				updateGraph();
			}
		});
		
		updatePortsComboBox();
		
		progressBarMemory.setMaximum(Connector.MEMORY_SPACE_FOR_RECORDS);
	}
	
	private void initTabForecast()
	{
		forecastTableModel = new ForecastRecordsTableModel(RecordsManager.getForecastRecords());
		forecastTableModel.addListener(graph::updateValues);
		
		buttonAddRecord.addActionListener(e -> {
			int now = (int) (new Date().getTime() / 1000);
			RecordsManager.getForecastRecords().add(new ForecastRecord(now, now, -1, 0, 0));
			RecordsManager.save();
			forecastTableModel.fireTableDataChanged();
			updateGraph();
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
		tableForecast.addKeyListener(new KeyListener()
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
				updateGraph();
			}
		});
		
		comboBoxSources.addItemListener(e -> updateSourceFilter());
		updateSourcesComboBox();
		
		buttonAddSource.addActionListener(e -> showSourceDialog(true));
		buttonEditSource.addActionListener(e -> showSourceDialog(false));
		buttonDeleteSource.addActionListener(e -> showSourceDeleteDialog());
	}
	
	private void initTabGraph()
	{
		spinnerModelNumber = new SpinnerNumberModel(5, 1, 100, 1);
		
		scrollBarOffset.addAdjustmentListener(e -> updateGraph());
		
		spinnerScale.setModel(spinnerModelNumber);
		spinnerScale.addChangeListener(e -> updateGraph());
		
		updateGraph();
	}
	
	private void updatePortsComboBox()
	{
		ArrayList<String> names = Connector.getPortsNames();
		String[] namesArray = names.toArray(new String[names.size()]);
		comboBoxPort.setModel(new DefaultComboBoxModel<>(namesArray));
	}
	
	private void updateSourcesComboBox()
	{
		String[] sources = new String[RecordsManager.getForecastSources().size()];
		RecordsManager.getForecastSources().toArray(sources);
		
		String[] names = new String[sources.length + 1];
		names[0] = "  Brak filtra";
		System.arraycopy(sources, 0, names, 1, sources.length);
		comboBoxSources.setModel(new DefaultComboBoxModel<>(names));
		
		updateSourceFilter();
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
	}
	
	private void updateGraph()
	{
		graph.setShowMeasurement(checkBoxMeasure.isSelected());
		graph.setShowForecast(checkBoxForecast.isSelected());
		graph.setShowTemperature(checkBoxTemperature.isSelected());
		graph.setShowHumidity(checkBoxHumidity.isSelected());
		graph.setDaysVisible((int) spinnerScale.getValue());
		int visible = scrollBarOffset.getVisibleAmount();
		int offset = visible < 100 ? scrollBarOffset.getValue() * 100 / (100 - visible) : 0;
		graph.setOffsetPercent(offset);
		graph.updateValues();
		
		int timeRatio = graph.getTimeScaleRatio();
		scrollBarOffset.setVisibleAmount(timeRatio);
		if(scrollBarOffset.getValue() + timeRatio > scrollBarOffset.getMaximum())
			scrollBarOffset.setValue(scrollBarOffset.getMaximum() - timeRatio);
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
		int option = JOptionPane.showOptionDialog(this,
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
		updateSourcesComboBox();
	}
	
	private void editCurrentSource(String newName)
	{
		checkCurrentSourceFilter();
		RecordsManager.getForecastSources().set(currentSourceFilter, newName);
		RecordsManager.save();
		updateSourcesComboBox();
		forecastTableModel.fireTableDataChanged();
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
		updateSourcesComboBox();
		forecastTableModel.fireTableDataChanged();
		updateGraph();
	}
	
	private void checkCurrentSourceFilter()
	{
		if(currentSourceFilter == -1 || currentSourceFilter >= RecordsManager.getForecastSources().size())
			throw new RuntimeException("Cannot delete this filter.");
	}
	
	private void onConnectClick()
	{
		if(connector == null)
		{
			connector = new Connector(Connector.getPortByName((String) comboBoxPort.getSelectedItem()), this);
			buttonSetTime.setEnabled(true);
			buttonSaveTime.setEnabled(true);
			buttonGetData.setEnabled(true);
			buttonReset.setEnabled(true);
			buttonConnect.setText("Rozłącz");
		}
		else disconnect();
	}
	
	private void onSetTimeClick()
	{
		connector.setTime();
	}
	
	private void onSaveTimeClick()
	{
		connector.saveTime();
	}
	
	private void onGetDataClick()
	{
		connector.getData();
	}
	
	private void onResetClick()
	{
		connector.reset();
		progressBarMemory.setValue(0);
	}
	
	private void onRefreshClick()
	{
		initPorts();
	}
	
	private void disconnect()
	{
		connector.disconnect();
		connector = null;
		buttonSetTime.setEnabled(false);
		buttonSaveTime.setEnabled(false);
		buttonGetData.setEnabled(false);
		buttonReset.setEnabled(false);
		buttonConnect.setText("Połącz");
	}
	
	@Override
	public void onPortInUse()
	{
		JOptionPane.showMessageDialog(this, "Port jest aktualnie w użyciu.", "Błąd", JOptionPane.ERROR_MESSAGE);
		disconnect();
	}
	
	@Override
	public void onError(String message)
	{
		JOptionPane.showMessageDialog(this, "Błąd: " + message, "Błąd", JOptionPane.ERROR_MESSAGE);
		disconnect();
	}
	
	@Override
	public void onDataReceiveTimeout()
	{
		JOptionPane.showMessageDialog(this, "Urządzenie nie odpowiada.", "Błąd", JOptionPane.ERROR_MESSAGE);
		disconnect();
	}
	
	@Override
	public void onDataReceive(ArrayList<Record> newRecords)
	{
		ArrayList<Record> records = RecordsManager.getMeasureRecords();
		newRecords.forEach(newRec ->
		{
			if(!records.contains(newRec)) records.add(newRec);
		});
		RecordsManager.save();
		measureTableModel.fireTableDataChanged();
		updateGraph();
		
		progressBarMemory.setValue(newRecords.size() * Connector.MEMORY_RECORD_SIZE);
	}
	
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(FormMain::new);
	}
}