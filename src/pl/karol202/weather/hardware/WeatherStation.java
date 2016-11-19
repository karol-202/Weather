package pl.karol202.weather.hardware;

import gnu.io.*;
import pl.karol202.weather.record.MeasureRecord;
import pl.karol202.weather.record.RecordsManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

public class WeatherStation implements SerialPortEventListener
{
	private class WaitForData implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				waitForData();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
				listener.onError(e.getMessage());
			}
		}
		
		private void waitForData() throws InterruptedException
		{
			Thread.sleep(TIMEOUT);
			if(waitForData)
			{
				listener.onDataReceiveTimeout();
				waitForData = false;
			}
		}
	}
	
	public static final int MEMORY_SPACE_FOR_RECORDS = 1024 - 8;
	public static final int MEMORY_RECORD_SIZE = 9;
	
	private final int TIMEOUT = 2000;
	private final int BAUD_RATE = 9600;
	private final int DATA_BITS = SerialPort.DATABITS_8;
	private final int STOP_BITS = SerialPort.STOPBITS_1;
	private final int PARITY = SerialPort.PARITY_NONE;
	
	private final int MESSAGE_SET_TIME = 1;
	private final int MESSAGE_SAVE_TIME = 2;
	private final int MESSAGE_GET_DATA = 3;
	private final int MESSAGE_RESET = 4;
	
	private CommPortIdentifier portId;
	private SerialPort port;
	private InputStream inputStream;
	private OutputStream outputStream;
	private ConnectionListener listener;
	private boolean waitForData;
	
	public WeatherStation(CommPortIdentifier portId, ConnectionListener listener)
	{
		this.portId = portId;
		this.listener = listener;
		try
		{
			connect();
		}
		catch(PortInUseException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onPortInUse();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void connect() throws Exception
	{
		port = (SerialPort) portId.open("Weather", TIMEOUT);
		port.setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
		inputStream = port.getInputStream();
		outputStream = port.getOutputStream();
		port.addEventListener(this);
		port.notifyOnDataAvailable(true);
	}
	
	public void disconnect()
	{
		try
		{
			tryDisconnect();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void tryDisconnect() throws IOException
	{
		inputStream.close();
		outputStream.close();
		port.removeEventListener();
		port.close();
	}
	
	public void checkConnection(ArrayList<CommPortIdentifier> allPorts)
	{
		if(allPorts.stream().map(CommPortIdentifier::getName).noneMatch(name -> name.equals(portId.getName())))
			listener.onError("Połączenie zostało zerwane");
	}
	
	public void setTime()
	{
		try
		{
			sendSetTime();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void sendSetTime() throws IOException
	{
		int time = (int) (new Date().getTime() / 1000);
		outputStream.write(MESSAGE_SET_TIME);
		outputStream.write(DataUtils.intToBytes(time));
		outputStream.write((int) RecordsManager.getTimeZone() + 128);
	}
	
	public void saveTime()
	{
		try
		{
			sendSaveTime();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void sendSaveTime() throws IOException
	{
		outputStream.write(MESSAGE_SAVE_TIME);
	}
	
	public void getData()
	{
		try
		{
			getDataInternal();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void getDataInternal() throws IOException
	{
		outputStream.write(MESSAGE_GET_DATA);
		waitForData = true;
		new Thread(new WaitForData()).start();
	}
	
	public void reset()
	{
		try
		{
			sendReset();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void sendReset() throws IOException
	{
		outputStream.write(MESSAGE_RESET);
	}
	
	@Override
	public void serialEvent(SerialPortEvent event)
	{
		if(event.getEventType() != SerialPortEvent.DATA_AVAILABLE || !waitForData) return;
		try
		{
			collectData();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void collectData() throws IOException
	{
		waitForData = false;
		ArrayList<MeasureRecord> records = new ArrayList<>();
		int length = inputStream.read();
		for(int i = 0; i < length; i++) readRecord(records);
		listener.onDataReceive(records);
	}
	
	private void readRecord(ArrayList<MeasureRecord> records) throws IOException
	{
		int time = DataUtils.readInt(inputStream);
		float temperature = DataUtils.readInt(inputStream) / 10f;
		float humidity = DataUtils.readInt(inputStream) / 10f;
		int rain = inputStream.read();
		records.add(new MeasureRecord(time, temperature, humidity, rain));
	}
}