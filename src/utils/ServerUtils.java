package utils;

public class ServerUtils {
	private static final long serialVersionUID = 1L;
	private static final int SERVER_PORT = 1234;
	private static final String SERVER_ADDRESS = "192.168.1.6";
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public static int getServerPort() {
		return SERVER_PORT;
	}
	public static String getServerAddress() {
		return SERVER_ADDRESS;
	}
	
}
