package pl.karol202.weather.hardware;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;

public class PortsManager
{
	private static ArrayList<CommPortIdentifier> ports = new ArrayList<>();

	public static void refreshPorts()
	{
		ports.clear();
		Enumeration portsEnum = CommPortIdentifier.getPortIdentifiers();
		while(portsEnum.hasMoreElements())
			ports.add((CommPortIdentifier) portsEnum.nextElement());
	}
	
	public static String[] getPortsNames()
	{
		return ports.stream().map(CommPortIdentifier::getName).toArray(String[]::new);
	}
	
	public static CommPortIdentifier getPortByName(String name)
	{
		return ports.stream().filter(port -> port.getName().equals(name)).findFirst().orElse(null);
	}
	
	public static ArrayList<CommPortIdentifier> getPorts()
	{
		return ports;
	}
}