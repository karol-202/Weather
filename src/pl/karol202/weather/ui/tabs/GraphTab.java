package pl.karol202.weather.ui.tabs;

import pl.karol202.weather.record.RecordsManager;
import pl.karol202.weather.ui.graph.Graph;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.text.DateFormat;
import java.util.Date;

public class GraphTab
{
	private SpinnerNumberModel spinnerModelNumber;
	private DateFormatter dateFormatter;
	private Thread dataUpdateThread;
	
	private Graph graph;
	private JScrollBar scrollBarOffset;
	private JSpinner spinnerScale;
	private JCheckBox checkBoxMeasurementTemperature;
	private JCheckBox checkBoxMeasurementHumidity;
	private JCheckBox checkBoxMeasurementRain;
	private JCheckBox checkBoxForecastTemperature;
	private JCheckBox checkBoxForecastHumidity;
	private JCheckBox checkBoxForecastRain;
	private JCheckBox checkBoxErrorTemperature;
	private JCheckBox checkBoxErrorHumidity;
	private JComboBox<String> comboBoxSource;
	private JFormattedTextField ftfForecastCreationTime;
	private JRadioButton radioFilterManual;
	private JRadioButton radioFilterNewest;
	
	public void init()
	{
		spinnerModelNumber = new SpinnerNumberModel(5, 1, 100, 1);
		dateFormatter = new DateFormatter(DateFormat.getDateTimeInstance());
		
		scrollBarOffset.addAdjustmentListener(e -> updateGraph(false));
		
		checkBoxMeasurementTemperature.addActionListener(e -> updateGraph(false));
		checkBoxMeasurementHumidity.addActionListener(e -> updateGraph(false));
		checkBoxMeasurementRain.addActionListener(e -> updateGraph(false));
		checkBoxForecastTemperature.addActionListener(e -> updateGraph(false));
		checkBoxForecastHumidity.addActionListener(e -> updateGraph(false));
		checkBoxForecastRain.addActionListener(e -> updateGraph(false));
		checkBoxErrorTemperature.addActionListener(e -> toggleForecastError());
		checkBoxErrorHumidity.addActionListener(e -> toggleForecastError());
		spinnerScale.setModel(spinnerModelNumber);
		spinnerScale.addChangeListener(e -> updateGraph(false));
		
		updateSources();
		comboBoxSource.addItemListener(e -> updateGraph(true));
		
		ftfForecastCreationTime.setFormatterFactory(new DefaultFormatterFactory(dateFormatter));
		ftfForecastCreationTime.addActionListener(e -> updateGraph(true));
		
		radioFilterManual.addActionListener(e -> updateForecastCreationFilter());
		radioFilterNewest.addActionListener(e -> updateForecastCreationFilter());
		
		updateGraph(true);
	}

	public void updateSources()
	{
		String[] sources = new String[RecordsManager.getForecastSources().size()];
		RecordsManager.getForecastSources().toArray(sources);
		comboBoxSource.setModel(new DefaultComboBoxModel<>(sources));
	}
	
	public void updateGraph()
	{
		updateGraph(true);
	}
	
	private void updateGraph(boolean updateData)
	{
		graph.setShowMeasurementTemperature(checkBoxMeasurementTemperature.isSelected());
		graph.setShowMeasurementHumidity(checkBoxMeasurementHumidity.isSelected());
		graph.setShowMeasurementRain(checkBoxMeasurementRain.isSelected());
		graph.setShowForecastTemperature(checkBoxForecastTemperature.isSelected());
		graph.setShowForecastHumidity(checkBoxForecastHumidity.isSelected());
		graph.setShowForecastRain(checkBoxForecastRain.isSelected());
		graph.setShowForecastErrorTemperature(checkBoxErrorTemperature.isSelected());
		graph.setShowForecastErrorHumidity(checkBoxErrorHumidity.isSelected());
		graph.setDaysVisible((int) spinnerScale.getValue());
		graph.setOffsetPercent(calcGraphOffset());
		graph.setCurrentSourceFilter(comboBoxSource.getSelectedIndex());
		graph.setForecastCreationTimeFilter(getForecastCreationTimeFilter());
		graph.setForecastCreationTimeFilterNewest(radioFilterNewest.isSelected());
		if(updateData) updateDataInNewThread();
		graph.updateValues();
		
		updateScrollBar();
	}
	
	private void updateDataInNewThread()
	{
		if(dataUpdateThread != null && dataUpdateThread.isAlive()) dataUpdateThread.interrupt();
		dataUpdateThread = new Thread(() -> graph.updateData());
		dataUpdateThread.start();
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
	
	private void updateForecastCreationFilter()
	{
		ftfForecastCreationTime.setEnabled(radioFilterManual.isSelected());
		
		updateGraph(true);
	}
	
	private void toggleForecastError()
	{
		boolean show = checkBoxErrorTemperature.isSelected() || checkBoxErrorHumidity.isSelected();
		checkBoxMeasurementTemperature.setSelected(!show);
		checkBoxMeasurementHumidity.setSelected(!show);
		checkBoxMeasurementRain.setSelected(!show);
		checkBoxForecastTemperature.setSelected(!show);
		checkBoxForecastHumidity.setSelected(!show);
		checkBoxForecastRain.setSelected(!show);
		checkBoxMeasurementTemperature.setEnabled(!show);
		checkBoxMeasurementHumidity.setEnabled(!show);
		checkBoxMeasurementRain.setEnabled(!show);
		checkBoxForecastTemperature.setEnabled(!show);
		checkBoxForecastHumidity.setEnabled(!show);
		checkBoxForecastRain.setEnabled(!show);
		
		updateGraph(false);
	}
	
	public void setGraph(Graph graph)
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
	
	public void setCheckBoxMeasurementTemperature(JCheckBox checkBox)
	{
		this.checkBoxMeasurementTemperature = checkBox;
	}
	
	public void setCheckBoxMeasurementHumidity(JCheckBox checkBox)
	{
		this.checkBoxMeasurementHumidity = checkBox;
	}
	
	public void setCheckBoxMeasurementRain(JCheckBox show)
	{
		this.checkBoxMeasurementRain = show;
	}
	
	public void setCheckBoxForecastTemperature(JCheckBox checkBox)
	{
		this.checkBoxForecastTemperature = checkBox;
	}
	
	public void setCheckBoxForecastHumidity(JCheckBox checkBox)
	{
		this.checkBoxForecastHumidity = checkBox;
	}
	
	public void setCheckBoxForecastRain(JCheckBox checkBox)
	{
		this.checkBoxForecastRain = checkBox;
	}
	
	public void setCheckBoxErrorTemperature(JCheckBox checkBox)
	{
		this.checkBoxErrorTemperature = checkBox;
	}
	
	public void setCheckBoxErrorHumidity(JCheckBox checkBox)
	{
		this.checkBoxErrorHumidity = checkBox;
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