package pl.karol202.weather.ui;

import pl.karol202.weather.hardware.Connector;
import pl.karol202.weather.hardware.Connector.ConnectionListener;
import pl.karol202.weather.record.ForecastRecord;
import pl.karol202.weather.record.Record;
import pl.karol202.weather.record.RecordsManager;
import pl.karol202.weather.ui.table.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;

public class FormMain extends JFrame implements ConnectionListener
{
	private Connector connector;
	private MeasureRecordsTableModel measureTableModel;
	private ForecastRecordsTableModel forecastTableModel;
	private SpinnerNumberModel spinnerModelNumber;
	
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
		Connector.init();
		ArrayList<String> names = Connector.getPortsNames();
		
		if(connector != null) if(names.stream().noneMatch(name -> name.equals(connector.getPortId().getName()))) disconnect();
		String[] namesArray = names.toArray(new String[names.size()]);
		comboBoxPort.setModel(new DefaultComboBoxModel<>(namesArray));
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
		tableMeasurement.setDefaultRenderer(Date.class, new RecordsTableRenderer());
		tableMeasurement.getColumnModel().getColumn(1).setMaxWidth(100);
		tableMeasurement.getColumnModel().getColumn(2).setMaxWidth(90);
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
		
		progressBarMemory.setMaximum(Connector.MEMORY_SPACE_FOR_RECORDS);
	}
	
	private void initTabForecast()
	{
		forecastTableModel = new ForecastRecordsTableModel(RecordsManager.getForecastRecords());
		forecastTableModel.addListener(graph::updateValues);
		
		buttonAddRecord.addActionListener(e -> {
			int now = (int) (new Date().getTime() / 1000);
			RecordsManager.getForecastRecords().add(new ForecastRecord(now, now, 0, 0));
			RecordsManager.save();
			forecastTableModel.fireTableDataChanged();
			updateGraph();
		});
		
		tableForecast.setModel(forecastTableModel);
		tableForecast.setRowSelectionAllowed(true);
		tableForecast.setColumnSelectionAllowed(false);
		tableForecast.getTableHeader().setReorderingAllowed(false);
		tableForecast.setDefaultRenderer(Date.class, new RecordsTableRenderer());
		tableForecast.setDefaultEditor(Date.class, new DateCellEditor());
		tableForecast.getColumnModel().getColumn(2).setMaxWidth(100);
		tableForecast.getColumnModel().getColumn(2).setCellEditor(new IntegerCellEditor(-128, 127));
		tableForecast.getColumnModel().getColumn(3).setMaxWidth(90);
		tableForecast.getColumnModel().getColumn(3).setCellEditor(new IntegerCellEditor(0, 100));
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
	}
	
	private void initTabGraph()
	{
		spinnerModelNumber = new SpinnerNumberModel(5, 1, 100, 1);
		
		scrollBarOffset.addAdjustmentListener(e -> updateGraph());
		
		spinnerScale.setModel(spinnerModelNumber);
		spinnerScale.addChangeListener(e -> updateGraph());
		
		updateGraph();
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