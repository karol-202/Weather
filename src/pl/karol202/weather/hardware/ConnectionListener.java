package pl.karol202.weather.hardware;

import pl.karol202.weather.record.Record;

import java.util.ArrayList;

public interface ConnectionListener
{
	void onPortInUse();
	
	void onError(String message);
	
	void onDataReceiveTimeout();
	
	void onDataReceive(ArrayList<Record> records);
}