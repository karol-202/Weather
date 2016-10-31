package pl.karol202.weather.ui;

import pl.karol202.weather.record.RecordsManager;

import javax.swing.*;

public class GraphTab
{
	private SpinnerNumberModel spinnerModelNumber;
	
	private GraphPanel graph;
	private JScrollBar scrollBarOffset;
	private JCheckBox checkBoxMeasure;
	private JCheckBox checkBoxForecast;
	private JCheckBox checkBoxTemperature;
	private JCheckBox checkBoxHumidity;
	private JSpinner spinnerScale;
	private JComboBox<String> comboBoxSource;
	
	public void init()
	{
		spinnerModelNumber = new SpinnerNumberModel(5, 1, 100, 1);
		
		scrollBarOffset.addAdjustmentListener(e -> updateGraph());
		
		checkBoxMeasure.addActionListener(e -> updateGraph());
		checkBoxForecast.addActionListener(e -> updateGraph());
		checkBoxTemperature.addActionListener(e -> updateGraph());
		checkBoxHumidity.addActionListener(e -> updateGraph());
		
		spinnerScale.setModel(spinnerModelNumber);
		spinnerScale.addChangeListener(e -> updateGraph());
		
		updateSources();
		comboBoxSource.addItemListener(e -> updateGraph());
		
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
		int visible = scrollBarOffset.getVisibleAmount();
		int offset = visible < 100 ? scrollBarOffset.getValue() * 100 / (100 - visible) : 0;
		graph.setOffsetPercent(offset);
		graph.setCurrentSourceFilter(comboBoxSource.getSelectedIndex());
		graph.updateValues();
		
		int timeRatio = graph.getTimeScaleRatio();
		scrollBarOffset.setVisibleAmount(timeRatio);
		if(scrollBarOffset.getValue() + timeRatio > scrollBarOffset.getMaximum())
			scrollBarOffset.setValue(scrollBarOffset.getMaximum() - timeRatio);
	}
	
	public void setGraph(GraphPanel graph)
	{
		this.graph = graph;
	}
	
	public void setScrollBarOffset(JScrollBar scrollBarOffset)
	{
		this.scrollBarOffset = scrollBarOffset;
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
	
	public void setSpinnerScale(JSpinner spinnerScale)
	{
		this.spinnerScale = spinnerScale;
	}
	
	public void setComboBoxSource(JComboBox<String> comboBoxSource)
	{
		this.comboBoxSource = comboBoxSource;
	}
}