/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Chat;

/**
 *
 * @author anago
 */
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;

/*
 * Paquete: Chat
 * Clase: Cliente (Fase 2: Descubrimiento por Broadcast)
 */

public class Cliente {

    private static final int PUERTO = 9876;
    private static final String DIRECCION_BROADCAST = "255.255.255.255"; 
    private static final int TIMEOUT_DISCOVER_MS = 5000; 

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Introduce tu nombre de usuario para el descubrimiento: ");
        String nombre = sc.nextLine();
        
        List<InetSocketAddress> amigosEncontrados = new ArrayList<>();
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_DISCOVER_MS);
            socket.setBroadcast(true); 

          
            InetAddress miIP = InetAddress.getLocalHost(); 
            System.out.println("Mi IP para filtrado: " + miIP.getHostAddress());

            InetAddress broadcastAddress = InetAddress.getByName(DIRECCION_BROADCAST);
            String mensaje = "@hola#" + nombre + "@";
            byte[] bufferEnvio = mensaje.getBytes();

            DatagramPacket paqueteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length,
                    broadcastAddress, PUERTO);

            socket.send(paqueteEnvio);
            System.out.println("Paquete de descubrimiento (Broadcast) enviado.");

            while (true) {
                byte[] bufferRecepcion = new byte[1024];
                DatagramPacket respuesta = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);

                try {
                    socket.receive(respuesta);
                    
                    InetAddress origen = respuesta.getAddress();
                    String mensajeRespuesta = new String(respuesta.getData(), 0, respuesta.getLength());

                    if (origen.equals(miIP)) {
                        System.out.println("   (Paquete ignorado: Es mi propia IP)");
                        continue;
                    }

                    Pattern pattern = Pattern.compile("@hola#(.+?)@");
                    Matcher matcher = pattern.matcher(mensajeRespuesta);

                    if (matcher.matches()) {
                        String nombreServidor = matcher.group(1);
                        InetSocketAddress amigo = new InetSocketAddress(origen, respuesta.getPort());
                        amigosEncontrados.add(amigo); // Añadir a la lista de "Amigos"
                        System.out.println("Descubierto: " + nombreServidor + " en " + amigo.toString());
                    } else {
                        System.out.println(" Respuesta de " + origen.getHostAddress() + " con formato no válido.");
                    }

                } catch (SocketTimeoutException e) {
                    break; 
                }
            }
            
            System.out.println("\n--- Resumen de Descubrimiento ---");
            if (amigosEncontrados.isEmpty()) {
                System.out.println("No se encontraron interlocutores en la red.");
            } else {
                for (int i = 0; i < amigosEncontrados.size(); i++) {
                    System.out.println("[" + i + "] " + amigosEncontrados.get(i).getHostName() + ":" + amigosEncontrados.get(i).getPort());
                }
            }
            System.out.println("---------------------------------");


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
            sc.close();
        }
    }
}