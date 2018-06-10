package pl.karol202.weather.hardware;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class PortsManager
{
	private static List<SerialPort> ports = new ArrayList<>();

	public static void refreshPorts()
	{
		SerialPort[] portsArray = SerialPort.getCommPorts();
		ports = Arrays.asList(portsArray);
	}
	
	public static String[] getPortsNames()
	{
		return ports.stream().map(SerialPort::getSystemPortName).toArray(String[]::new);
	}
	
	public static SerialPort getPortByName(String name)
	{
		return ports.stream().filter(port -> port.getSystemPortName().equals(name)).findFirst().orElse(null);
	}
	
	public static List<SerialPort> getPorts()
	{
		return ports;
	}
}