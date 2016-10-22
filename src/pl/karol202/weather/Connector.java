package pl.karol202.weather;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

public class Connector implements SerialPortEventListener
{
	public interface ConnectionListener
	{
		void onPortInUse();
		
		void onError(String message);
		
		void onDataReceiveTimeout();
		
		void onDataReceive(ArrayList<Record> records);
	}
	
	private class WaitForData implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				Thread.sleep(TIMEOUT);
				if(waitForData)
				{
					listener.onDataReceiveTimeout();
					waitForData = false;
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private final int TIMEOUT = 2000;
	private final int BAUD_RATE = 9600;
	private final int DATA_BITS = SerialPort.DATABITS_8;
	private final int STOP_BITS = SerialPort.STOPBITS_1;
	private final int PARITY = SerialPort.PARITY_NONE;
	
	private final int MESSAGE_SET_TIME = 1;
	private final int MESSAGE_SAVE_TIME = 2;
	private final int MESSAGE_GET_DATA = 3;
	private final int MESSAGE_RESET = 4;
	
	private static ArrayList<CommPortIdentifier> ports;
	
	private CommPortIdentifier portId;
	private SerialPort port;
	private InputStream inputStream;
	private OutputStream outputStream;
	private ConnectionListener listener;
	private boolean waitForData;
	
	public Connector(CommPortIdentifier portId, ConnectionListener listener)
	{
		this.portId = portId;
		this.listener = listener;
		
		try
		{
			port = (SerialPort) portId.open("Weather", TIMEOUT);
			port.setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
			inputStream = port.getInputStream();
			outputStream = port.getOutputStream();
			port.addEventListener(this);
			port.notifyOnDataAvailable(true);
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
	
	public static void init()
	{
		ports = new ArrayList<>();
		Enumeration portsEnum = CommPortIdentifier.getPortIdentifiers();
		while(portsEnum.hasMoreElements())
			ports.add((CommPortIdentifier) portsEnum.nextElement());
	}
	
	public void disconnect()
	{
		port.removeEventListener();
		port.close();
	}
	
	public void setTime()
	{
		try
		{
			System.out.println(new Date().getTime());
			long time = new Date().getTime() / 1000;
			outputStream.write(MESSAGE_SET_TIME);
			outputStream.write(longToBytes(time));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	public void saveTime()
	{
		try
		{
			outputStream.write(MESSAGE_SAVE_TIME);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	public void getData()
	{
		try
		{
			outputStream.write(MESSAGE_GET_DATA);
			waitForData = true;
			new Thread(new WaitForData()).start();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	public void reset()
	{
		try
		{
			outputStream.write(MESSAGE_RESET);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			if(listener != null) listener.onError(e.getMessage());
		}
	}
	
	@Override
	public void serialEvent(SerialPortEvent event)
	{
		if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE && waitForData)
		{
			try
			{
				waitForData = false;
				ArrayList<Record> records = Record.decodeRecord(inputStream);
				listener.onDataReceive(records);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				if(listener != null) listener.onError(e.getMessage());
			}
		}
	}
	
	public static ArrayList<String> getPortsNames()
	{
		ArrayList<String> names = new ArrayList<>();
		ports.forEach(port -> names.add(port.getName()));
		return names;
	}
	
	public static CommPortIdentifier getPortByName(String name)
	{
		return ports.stream().filter(port -> port.getName().equals(name)).findFirst().orElse(null);
	}
	
	public CommPortIdentifier getPortId()
	{
		return portId;
	}
	
	private byte[] longToBytes(long number)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((number >> 24) & 0xff);
		bytes[1] = (byte) ((number >> 16) & 0xff);
		bytes[2] = (byte) ((number >> 8 ) & 0xff);
		bytes[3] = (byte)  (number        & 0xff);
		System.out.println(bytes[0]);
		System.out.println(bytes[1]);
		System.out.println(bytes[2]);
		System.out.println(bytes[3]);
		return bytes;
	}
}