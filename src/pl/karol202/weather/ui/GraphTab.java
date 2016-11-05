package pl.karol202.weather.ui;

import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.text.DateFormat;
import java.util.Date;

public class GraphTab
{
	private SpinnerNumberModel spinnerModelNumber;
	private DateFormatter dateFormatter;
	
	private GraphPanel graph;
	private JScrollBar scrollBarOffset;
	private JSpinner spinnerScale;
	private JCheckBox checkBoxMeasure;
	private JCheckBox checkBoxForecast;
	private JCheckBox checkBoxTemperature;
	private JCheckBox checkBoxHumidity;
	private JComboBox<String> comboBoxSource;
	private JFormattedTextField ftfForecastCreationTime;
	private JRadioButton radioFilterManual;
	private JRadioButton radioFilterNewest;
	
	public void init()
	{
		spinnerModelNumber = new SpinnerNumberModel(5, 1, 100, 1);
		dateFormatter = new DateFormatter(DateFormat.getDateTimeInstance());
		
		scrollBarOffset.addAdjustmentListener(e -> updateGraph());
		
		checkBoxMeasure.addActionListener(e -> updateGraph());
		checkBoxForecast.addActionListener(e -> toggleForecast(checkBoxForecast.isSelected()));
		checkBoxTemperature.addActionListener(e -> updateGraph());
		checkBoxHumidity.addActionListener(e -> updateGraph());
		
		spinnerScale.setModel(spinnerModelNumber);
		spinnerScale.addChangeListener(e -> updateGraph());
		
		updateSources();
		comboBoxSource.addItemListener(e -> updateGraph());
		
		ftfForecastCreationTime.setFormatterFactory(new DefaultFormatterFactory(dateFormatter));
		ftfForecastCreationTime.addActionListener(e -> updateGraph());
		
		radioFilterManual.addActionListener(e -> updateForecastCreationFilter());
		radioFilterNewest.addActionListener(e -> updateForecastCreationFilter());
		
		updateGraph();
	}

	public void updateSources()
	{
		String[] sources = new String[RecordsManager.getForecastSources().size()];
		RecordsManager.getForecastSources().toArray(sources);
		comboBoxSource.setModel(new DefaultComboBoxModel<>(sources));
	}
	
	public void updateGraph()
	{
		graph.setShowMeasurement(checkBoxMeasure.isSelected());
		graph.setShowForecast(checkBoxForecast.isSelected());
		graph.setShowTemperature(checkBoxTemperature.isSelected());
		graph.setShowHumidity(checkBoxHumidity.isSelected());
		graph.setDaysVisible((int) spinnerScale.getValue());
		graph.setOffsetPercent(calcGraphOffset());
		graph.setCurrentSourceFilter(comboBoxSource.getSelectedIndex());
		graph.setForecastCreationTimeFilter(getForecastCreationTimeFilter());
		graph.setForecastCreationTimeFilterNewest(radioFilterNewest.isSelected());
		graph.updateValues();
		
		updateScrollBar();
	}
	
	private int calcGraphOffset()
	{
		int visible = scrollBarOffset.getVisibleAmount();
		return visible < 100 ? scrollBarOffset.getValue() * 100 / (100 - visible) : 0;
	}
	
	private int getForecastCreationTimeFilter()
	{
		Object value = ftfForecastCreationTime.getValue();
		if(value == null) return -1;
		long timeInMillis = ((Date) value).getTime();
		return (int) (timeInMillis / 1000);
	}
	
	private void updateScrollBar()
	{
		int timeRatio = graph.getTimeScaleRatio();
		scrollBarOffset.setVisibleAmount(timeRatio);
		if(scrollBarOffset.getValue() + timeRatio > scrollBarOffset.getMaximum())
			scrollBarOffset.setValue(scrollBarOffset.getMaximum() - timeRatio);
	}
	
	private void toggleForecast(boolean enabled)
	{
		comboBoxSource.setEnabled(enabled);
		radioFilterManual.setEnabled(enabled);
		radioFilterNewest.setEnabled(enabled);
		
		updateForecastCreationFilter();
		updateGraph();
	}
	
	private void updateForecastCreationFilter()
	{
		ftfForecastCreationTime.setEnabled(radioFilterManual.isSelected());
		
		updateGraph();
	}
	
	public void setGraph(GraphPanel graph)
	{
		this.graph = graph;
	}
	
	public void setScrollBarOffset(JScrollBar scrollBarOffset)
	{
		this.scrollBarOffset = scrollBarOffset;
	}
	
	public void setSpinnerScale(JSpinner spinnerScale)
	{
		this.spinnerScale = spinnerScale;
	}
	
	public void setCheckBoxMeasure(JCheckBox checkBoxMeasure)
	{
		this.checkBoxMeasure = checkBoxMeasure;
	}
	
	public void setCheckBoxForecast(JCheckBox checkBoxForecast)
	{
		this.checkBoxForecast = checkBoxForecast;
	}
	
	public void setCheckBoxTemperature(JCheckBox checkBoxTemperature)
	{
		this.checkBoxTemperature = checkBoxTemperature;
	}
	
	public void setCheckBoxHumidity(JCheckBox checkBoxHumidity)
	{
		this.checkBoxHumidity = checkBoxHumidity;
	}
	
	public void setComboBoxSource(JComboBox<String> comboBoxSource)
	{
		this.comboBoxSource = comboBoxSource;
	}
	
	public void setFtfForecastCreationTime(JFormattedTextField ftf)
	{
		this.ftfForecastCreationTime = ftf;
	}
	
	public void setRadioFilterManual(JRadioButton radioFilterManual)
	{
		this.radioFilterManual = radioFilterManual;
	}
	
	public void setRadioFilterNewest(JRadioButton radioFilterNewest)
	{
		this.radioFilterNewest = radioFilterNewest;
	}
}