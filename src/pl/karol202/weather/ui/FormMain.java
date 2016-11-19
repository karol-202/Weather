package pl.karol202.weather.ui;

import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;

public class FormMain extends JFrame
{
	private MeasurementTab measurementTab;
	private ForecastTab forecastTab;
	private GraphTab graphTab;
	
	private JPanel root;
	
	private JTable tableMeasurement;
	private JButton buttonSetTime;
	private JButton buttonSaveTime;
	private JButton buttonGetData;
	private JButton buttonReset;
	private JButton buttonConnect;
	private JButton buttonRefresh;
	private JComboBox<String> comboBoxPort;
	private JProgressBar progressBarMemory;
	
	private JTable tableForecast;
	private JButton buttonAddRecord;
	private JComboBox<String> comboBoxSources;
	private JButton buttonAddSource;
	private JButton buttonEditSource;
	private JButton buttonDeleteSource;
	
	private GraphPanel graph;
	private JScrollBar scrollBarOffset;
	private JCheckBox checkBoxMeasure;
	private JCheckBox checkBoxForecast;
	private JCheckBox checkBoxMeasurementTemperature;
	private JCheckBox checkBoxMeasurementHumidity;
	private JSpinner spinnerScale;
	private JComboBox<String> comboBoxSourceGraph;
	private JFormattedTextField ftfForecastCreationTime;
	private JRadioButton radioFilterManual;
	private JRadioButton radioFilterNewest;
	private JCheckBox checkBoxError;
	private JSpinner spinnerTimeZone;
	private JCheckBox checkBoxForecastTemperature;
	private JCheckBox checkBoxForecastHumidity;
	private JCheckBox checkBoxErrorTemperature;
	private JCheckBox checkBoxErrorHumidity;
	
	private FormMain()
	{
		super("Weather");
		setContentPane(root);
		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocation(500, 200);
		setResizable(true);
		setVisible(true);
		
		RecordsManager.init();
		initTabs();
	}
	
	private void initTabs()
	{
		initMeasurementTab();
		initForecastTab();
		initGraphTab();
	}
	
	private void initMeasurementTab()
	{
		measurementTab = new MeasurementTab(this);
		measurementTab.setTableMeasurement(tableMeasurement);
		measurementTab.setButtonSetTime(buttonSetTime);
		measurementTab.setButtonSaveTime(buttonSaveTime);
		measurementTab.setButtonGetData(buttonGetData);
		measurementTab.setButtonReset(buttonReset);
		measurementTab.setButtonConnect(buttonConnect);
		measurementTab.setButtonRefresh(buttonRefresh);
		measurementTab.setComboBoxPort(comboBoxPort);
		measurementTab.setProgressBarMemory(progressBarMemory);
		measurementTab.setSpinnerTimeZone(spinnerTimeZone);
		measurementTab.init();
	}
	
	private void initForecastTab()
	{
		forecastTab = new ForecastTab(this);
		forecastTab.setTableForecast(tableForecast);
		forecastTab.setButtonAddRecord(buttonAddRecord);
		forecastTab.setComboBoxSources(comboBoxSources);
		forecastTab.setButtonAddSource(buttonAddSource);
		forecastTab.setButtonEditSource(buttonEditSource);
		forecastTab.setButtonDeleteSource(buttonDeleteSource);
		forecastTab.init();
	}
	
	private void initGraphTab()
	{
		graphTab = new GraphTab();
		graphTab.setGraph(graph);
		graphTab.setScrollBarOffset(scrollBarOffset);
		graphTab.setSpinnerScale(spinnerScale);
		graphTab.setCheckBoxMeasurementTemperature(checkBoxMeasurementTemperature);
		graphTab.setCheckBoxMeasurementHumidity(checkBoxMeasurementHumidity);
		graphTab.setCheckBoxForecastTemperature(checkBoxForecastTemperature);
		graphTab.setCheckBoxForecastHumidity(checkBoxForecastHumidity);
		graphTab.setCheckBoxErrorTemperature(checkBoxErrorTemperature);
		graphTab.setCheckBoxErrorHumidity(checkBoxErrorHumidity);
		graphTab.setComboBoxSource(comboBoxSourceGraph);
		graphTab.setFtfForecastCreationTime(ftfForecastCreationTime);
		graphTab.setRadioFilterManual(radioFilterManual);
		graphTab.setRadioFilterNewest(radioFilterNewest);
		graphTab.init();
	}
	
	public void updateSources()
	{
		if(graphTab != null) graphTab.updateSources();
	}
	
	public void updateGraph()
	{
		if(graphTab != null) graphTab.updateGraph();
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