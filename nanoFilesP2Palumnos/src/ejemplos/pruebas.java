package ejemplos;

import java.net.InetSocketAddress;

public class pruebas {
	public static void main(String[] args) {
		InetSocketAddress address = new InetSocketAddress("127.0.0.1",8080);
		String ip = address.getHostString();
		System.out.println(ip);
	}
}
