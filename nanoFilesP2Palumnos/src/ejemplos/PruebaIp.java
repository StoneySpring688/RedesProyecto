package ejemplos;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PruebaIp {
	public static void main(String[] args) {
        try {
            // Obtener la dirección IP local del dispositivo
            InetAddress localIP = InetAddress.getLocalHost();

            // Crear un InetSocketAddress con la dirección IP local y un puerto específico
            InetSocketAddress localSocketAddress = new InetSocketAddress(localIP, 9999); // Cambia 9999 al puerto deseado

            // Imprimir la dirección IP y el puerto del dispositivo
            System.out.println("Dirección IP local del dispositivo: " + localSocketAddress.getAddress().getHostAddress());
            System.out.println("Puerto local del dispositivo: " + localSocketAddress.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

