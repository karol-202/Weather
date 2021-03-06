package pl.karol202.weather.ui.tabs;

import pl.karol202.weather.hardware.ConnectionListener;
import pl.karol202.weather.hardware.PortsManager;
import pl.karol202.weather.hardware.WeatherStation;
import pl.karol202.weather.record.MeasureRecord;
import pl.karol202.weather.record.RecordsManager;
import pl.karol202.weather.ui.FormMain;
import pl.karol202.weather.ui.table.model.MeasureRecordsTableModel;
import pl.karol202.weather.ui.table.renderer.DateCellRenderer;
import pl.karol202.weather.ui.table.renderer.UnitCellRenderer;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasurementTab implements ConnectionListener
{
	private FormMain parent;
	private WeatherStation weatherStation;
	private MeasureRecordsTableModel measureTableModel;
	private SpinnerNumberModel spinnerNumberModel;
	
	private JTable tableMeasurement;
	private JButton buttonSetTime;
	private JButton buttonSaveTime;
	private JButton buttonGetData;
	private JButton buttonReset;
	private JButton buttonConnect;
	private JButton buttonRefresh;
	private JComboBox<String> comboBoxPort;
	private JProgressBar progressBarMemory;
	private JSpinner spinnerTimeZone;
	
	public MeasurementTab(FormMain parent)
	{
		this.parent = parent;
	}
	
	public void init()
	{
		measureTableModel = new MeasureRecordsTableModel();
		spinnerNumberModel = new SpinnerNumberModel(RecordsManager.getTimeZone(), -12, 14, 1);
		initPorts();

		tableMeasurement.setModel(measureTableModel);
		tableMeasurement.setRowSelectionAllowed(true);
		tableMeasurement.setColumnSelectionAllowed(false);
		tableMeasurement.getTableHeader().setReorderingAllowed(false);
		tableMeasurement.setDefaultRenderer(Date.class, new DateCellRenderer());
		tableMeasurement.getColumnModel().getColumn(1).setMaxWidth(100);
		tableMeasurement.getColumnModel().getColumn(1).setCellRenderer(new UnitCellRenderer("°C"));
		tableMeasurement.getColumnModel().getColumn(2).setMaxWidth(80);
		tableMeasurement.getColumnModel().getColumn(2).setCellRenderer(new UnitCellRenderer("%"));
		tableMeasurement.getColumnModel().getColumn(3).setMaxWidth(100);
		tableMeasurement.getColumnModel().getColumn(3).setCellRenderer(new UnitCellRenderer("%"));
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
				parent.updateGraph();
			}
		});
		
		buttonSetTime.addActionListener(e -> onSetTimeClick());
		buttonSaveTime.addActionListener(e -> onSaveTimeClick());
		buttonGetData.addActionListener(e -> onGetDataClick());
		buttonReset.addActionListener(e -> onResetClick());
		buttonConnect.addActionListener(e -> onConnectClick());
		buttonRefresh.addActionListener(e -> onRefreshClick());
		
		progressBarMemory.setMaximum(WeatherStation.MEMORY_SPACE_FOR_RECORDS);
		
		spinnerTimeZone.setModel(spinnerNumberModel);
		spinnerTimeZone.addChangeListener(e -> updateTimeZone());
	}
	
	private void initPorts()
	{
		PortsManager.refreshPorts();
		if(weatherStation != null) weatherStation.checkConnection(PortsManager.getPorts());
		updatePortsComboBox();
	}
	
	private void updatePortsComboBox()
	{
		comboBoxPort.setModel(new DefaultComboBoxModel<>(PortsManager.getPortsNames()));
	}
	
	private void updateTimeZone()
	{
		RecordsManager.setTimeZone((float) (double) spinnerNumberModel.getNumber());
		RecordsManager.save();
	}
	
	private void onConnectClick()
	{
		if(weatherStation == null)
		{
			weatherStation = new WeatherStation(PortsManager.getPortByName((String) comboBoxPort.getSelectedItem()), this);
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
		weatherStation.setTime();
	}
	
	private void onSaveTimeClick()
	{
		weatherStation.saveTime();
	}
	
	private void onGetDataClick()
	{
		weatherStation.getData();
	}
	
	private void onResetClick()
	{
		weatherStation.reset();
		progressBarMemory.setValue(0);
	}
	
	private void onRefreshClick()
	{
		initPorts();
	}
	
	private void disconnect()
	{
		weatherStation.disconnect();
		weatherStation = null;
		buttonSetTime.setEnabled(false);
		buttonSaveTime.setEnabled(false);
		buttonGetData.setEnabled(false);
		buttonReset.setEnabled(false);
		buttonConnect.setText("Połącz");
	}
	
	@Override
	public void onError(String message)
	{
		JOptionPane.showMessageDialog(parent, "Błąd: " + message, "Błąd", JOptionPane.ERROR_MESSAGE);
		disconnect();
	}
	
	@Override
	public void onDataReceiveTimeout()
	{
		JOptionPane.showMessageDialog(parent, "Urządzenie nie odpowiada.", "Błąd", JOptionPane.ERROR_MESSAGE);
		disconnect();
	}
	
	@Override
	public void onDataReceive(List<MeasureRecord> newRecords)
	{
		ArrayList<MeasureRecord> records = RecordsManager.getMeasureRecords();
		newRecords.forEach(newRec ->
		{
			if(!records.contains(newRec)) records.add(newRec);
		});
		RecordsManager.save();
		measureTableModel.fireTableDataChanged();
		parent.updateGraph();
		
		progressBarMemory.setValue(newRecords.size() * WeatherStation.MEMORY_RECORD_SIZE);
	}
	
	public void setTableMeasurement(JTable tableMeasurement)
	{
		this.tableMeasurement = tableMeasurement;
	}
	
	public void setButtonSetTime(JButton buttonSetTime)
	{
		this.buttonSetTime = buttonSetTime;
	}
	
	public void setButtonSaveTime(JButton buttonSaveTime)
	{
		this.buttonSaveTime = buttonSaveTime;
	}
	
	public void setButtonGetData(JButton buttonGetData)
	{
		this.buttonGetData = buttonGetData;
	}
	
	public void setButtonReset(JButton buttonReset)
	{
		this.buttonReset = buttonReset;
	}
	
	public void setButtonConnect(JButton buttonConnect)
	{
		this.buttonConnect = buttonConnect;
	}
	
	public void setButtonRefresh(JButton buttonRefresh)
	{
		this.buttonRefresh = buttonRefresh;
	}
	
	public void setComboBoxPort(JComboBox<String> comboBoxPort)
	{
		this.comboBoxPort = comboBoxPort;
	}
	
	public void setProgressBarMemory(JProgressBar progressBarMemory)
	{
		this.progressBarMemory = progressBarMemory;
	}
	
	public void setSpinnerTimeZone(JSpinner spinnerTimeZone)
	{
		this.spinnerTimeZone = spinnerTimeZone;
	}
}