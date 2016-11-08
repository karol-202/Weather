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
	private JCheckBox checkBoxTemperature;
	private JCheckBox checkBoxHumidity;
	private JSpinner spinnerScale;
	private JComboBox<String> comboBoxSourceGraph;
	private JFormattedTextField ftfForecastCreationTime;
	private JRadioButton radioFilterManual;
	private JRadioButton radioFilterNewest;
	private JCheckBox checkBoxError;
	
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
		graphTab.setCheckBoxMeasure(checkBoxMeasure);
		graphTab.setCheckBoxForecast(checkBoxForecast);
		graphTab.setCheckBoxError(checkBoxError);
		graphTab.setCheckBoxTemperature(checkBoxTemperature);
		graphTab.setCheckBoxHumidity(checkBoxHumidity);
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