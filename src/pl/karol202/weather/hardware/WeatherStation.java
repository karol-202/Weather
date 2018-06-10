package pl.karol202.weather.hardware;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import pl.karol202.weather.record.MeasureRecord;
import pl.karol202.weather.record.RecordsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherStation implements SerialPortDataListener
{
	private static class Timeout implements Runnable
	{
		interface TimeoutListener
		{
			void onTimeout();
		}

		private int timeout;
		private TimeoutListener listener;

		private boolean waiting;

		Timeout(int timeout, TimeoutListener listener)
		{
			this.timeout = timeout;
			this.listener = listener;
		}

		@Override
		public void run()
		{
			waiting = true;
			try
			{
				waitAndCheck();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		private void waitAndCheck() throws InterruptedException
		{
			Thread.sleep(timeout);
			if(waiting)
			{
				listener.onTimeout();
				waiting = false;
			}
		}

		boolean isWaiting()
		{
			return waiting;
		}

		void stop()
		{
			waiting = false;
		}
	}
	
	public static final int MEMORY_SPACE_FOR_RECORDS = 1024 - 8;
	public static final int MEMORY_RECORD_SIZE = 9;
	
	private final int TIMEOUT = 2000;
	private final int BAUD_RATE = 9600;
	private final int DATA_BITS = 8;
	private final int STOP_BITS = SerialPort.ONE_STOP_BIT;
	private final int PARITY = SerialPort.NO_PARITY;
	
	private final int MESSAGE_SET_TIME = 1;
	private final int MESSAGE_SAVE_TIME = 2;
	private final int MESSAGE_GET_DATA = 3;
	private final int MESSAGE_RESET = 4;

	private SerialPort port;
	private ConnectionListener listener;
	private Timeout timeout;
	
	public WeatherStation(SerialPort port, ConnectionListener listener)
	{
		this.port = port;
		this.listener = listener;
		this.timeout = new Timeout(TIMEOUT, () -> {
			if(listener != null) listener.onDataReceiveTimeout();
		});

		try
		{
			connect();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	private void connect() throws IOException
	{
		port.setBaudRate(BAUD_RATE);
		port.setNumDataBits(DATA_BITS);
		port.setNumStopBits(STOP_BITS);
		port.setParity(PARITY);
		if(!port.openPort()) throw new IOException("Cannot open port");
		port.addDataListener(this);
	}
	
	public void disconnect()
	{
		port.removeDataListener();
		port.closePort();
	}
	
	public void checkConnection(List<SerialPort> allPorts)
	{
		if(allPorts.stream().noneMatch(p -> p.getSystemPortName().equals(port.getSystemPortName())))
			listener.onError("Połączenie zostało zerwane");
	}
	
	public void setTime()
	{
		int time = (int) (new Date().getTime() / 1000);
		write(MESSAGE_SET_TIME);
		write(DataUtils.intToBytes(time));
		write((int) RecordsManager.getTimeZone() + 128);
	}
	
	public void saveTime()
	{
		write(MESSAGE_SAVE_TIME);
	}
	
	public void getData()
	{
		write(MESSAGE_GET_DATA);
		new Thread(timeout).start();
	}
	
	public void reset()
	{
		write(MESSAGE_RESET);
	}

	@Override
	public int getListeningEvents()
	{
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent)
	{
		if(timeout.isWaiting()) collectData();
	}

	private void collectData()
	{
		List<MeasureRecord> records = new ArrayList<>();
		int length = read();
		for(int i = 0; i < length; i++) records.add(readRecord());
		timeout.stop();
		listener.onDataReceive(records);
	}

	private MeasureRecord readRecord()
	{
		int time = readInt();
		float temperature = readInt() / 10f;
		float humidity = readInt() / 10f;
		int rain = read();
		return new MeasureRecord(time, temperature, humidity, rain);
	}

	private byte read()
	{
		while(timeout.isWaiting() && port.bytesAvailable() < 1) Thread.yield();

		byte[] buffer = new byte[1];
		port.readBytes(buffer, 1);
		return buffer[0];
	}

	private int readInt()
	{
		while(timeout.isWaiting() && port.bytesAvailable() < 4) Thread.yield();

		byte[] buffer = new byte[4];
		port.readBytes(buffer, 4);
		return DataUtils.bytesToInt(buffer);
	}

	private void write(int b)
	{
		port.writeBytes(new byte[] { (byte) b }, 1);
	}

	private void write(byte[] bytes)
	{
		port.writeBytes(bytes, bytes.length);
	}
}