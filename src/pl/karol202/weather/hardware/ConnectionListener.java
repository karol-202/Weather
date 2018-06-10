package pl.karol202.weather.hardware;

import pl.karol202.weather.record.MeasureRecord;

import java.util.ArrayList;
import java.util.List;

public interface ConnectionListener
{
	void onError(String message);
	
	void onDataReceiveTimeout();
	
	void onDataReceive(List<MeasureRecord> records);
}