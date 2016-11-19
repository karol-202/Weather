package pl.karol202.weather.ui.table.model;

import pl.karol202.weather.record.MeasureRecord;
import pl.karol202.weather.record.RecordsManager;

import java.util.Date;

public class MeasureRecordsTableModel extends RecordsTableModel<MeasureRecord>
{
	private static final String[] header = new String[] { "Czas", "Temperatura", "Wilgotność", "Moc opadów"};
	
	public MeasureRecordsTableModel()
	{
		super(header, RecordsManager.getMeasureRecords());
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		MeasureRecord record = getData().get(rowIndex);
		switch(columnIndex)
		{
		case 0:
			long timeInMillis = ((long) record.getTimeInSeconds()) * 1000;
			return new Date(timeInMillis);
		case 1:
			return record.getTemperature();
		case 2:
			return record.getHumidity();
		case 3:
			return record.getRainLevel();
		}
		return null;
	}
}
