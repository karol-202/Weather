package pl.karol202.weather;

import pl.karol202.weather.Connector.ConnectionListener;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;

public class FormMain extends JFrame implements ConnectionListener
{
	private Connector connector;
	private RecordsTableModel tableModelMeasure;
	private ForecastRecordsTableModel tableModelForecast;
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
		
		tableModelMeasure = new RecordsTableModel(RecordsManager.getRecordsMeasure());
		//tableModelMeasure.addTableModelListener(e -> RecordsManager.save());
		
		tableModelForecast = new ForecastRecordsTableModel(RecordsManager.getRecordsForecast());
		
		spinnerModelNumber = new SpinnerNumberModel(5, 1, 100, 1);
		
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
		
		tableMeasurement.setModel(tableModelMeasure);
		tableMeasurement.setRowSelectionAllowed(true);
		tableMeasurement.setColumnSelectionAllowed(false);
		tableMeasurement.getTableHeader().setReorderingAllowed(false);
		tableMeasurement.setDefaultRenderer(Date.class, new RecordsTableRenderer());
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
					RecordsManager.getRecordsMeasure().remove(row - deleted);
					deleted++;
				}
				RecordsManager.save();
				tableModelMeasure.fireTableDataChanged();
				updateGraph();
			}
		});
		
		buttonAddRecord.addActionListener(e -> {
			RecordsManager.getRecordsForecast().add(new Record((int) (new Date().getTime() / 1000), 0, 0));
			RecordsManager.save();
			tableModelForecast.fireTableDataChanged();
			updateGraph();
		});
		
		tableForecast.setModel(tableModelForecast);
		tableForecast.setRowSelectionAllowed(true);
		tableForecast.setColumnSelectionAllowed(false);
		tableForecast.getTableHeader().setReorderingAllowed(false);
		tableForecast.setDefaultRenderer(Date.class, new RecordsTableRenderer());
		tableForecast.setDefaultEditor(Date.class, new DateCellEditor());
		tableForecast.getColumnModel().getColumn(1).setCellEditor(new IntegerCellEditor(-128, 127));
		tableForecast.getColumnModel().getColumn(2).setCellEditor(new IntegerCellEditor(0, 100));
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
					RecordsManager.getRecordsForecast().remove(row - deleted);
					deleted++;
				}
				RecordsManager.save();
				tableModelForecast.fireTableDataChanged();
				updateGraph();
			}
		});
		
		scrollBarOffset.addAdjustmentListener(e -> updateGraph());
		
		spinnerScale.setModel(spinnerModelNumber);
		spinnerScale.addChangeListener(e -> updateGraph());
		
		updateGraph();
	}
	
	private void initPorts()
	{
		Connector.init();
		ArrayList<String> names = Connector.getPortsNames();
		
		if(connector != null) if(names.stream().noneMatch(name -> name.equals(connector.getPortId().getName()))) disconnect();
		String[] namesArray = names.toArray(new String[names.size()]);
		comboBoxPort.setModel(new DefaultComboBoxModel<>(namesArray));
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
		ArrayList<Record> records = RecordsManager.getRecordsMeasure();
		newRecords.forEach(newRec ->
		{
			if(!records.contains(newRec)) records.add(newRec);
		});
		RecordsManager.save();
		tableModelMeasure.fireTableDataChanged();
		updateGraph();
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