/*
 * Paquete: Chat
 * Clase: Main (Fase 3 Modificada: Puertos Dinámicos)
 */
package Chat;

import java.net.*;
import java.util.Scanner;

public class Main {
    
    private static final String COMANDO_SALIR = "/salir";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DatagramSocket chatSocket = null;
        
        int puertoEscuchaPropio = -1;
        int puertoDestino = -1;

        try {
            System.out.print("Nombre de Usuario: ");
            String miNombre = scanner.nextLine();

            System.out.print(" Puerto de Escucha Propio (ej. 9876): ");
            if (scanner.hasNextInt()) {
                puertoEscuchaPropio = scanner.nextInt();
                scanner.nextLine(); 
            } else {
                throw new IllegalArgumentException("Puerto de escucha no válido.");
            }
            
            System.out.print("IP del Interlocutor (ej. localhost): ");
            String ipInterlocutorStr = scanner.nextLine();
            
            System.out.print("Puerto del Interlocutor (DESTINO, ej. 9877): ");
            if (scanner.hasNextInt()) {
                puertoDestino = scanner.nextInt();
                scanner.nextLine();
            } else {
                throw new IllegalArgumentException("Puerto de destino no válido.");
            }
            
            InetAddress ipInterlocutor = InetAddress.getByName(ipInterlocutorStr);
            
            chatSocket = new DatagramSocket(puertoEscuchaPropio);
            System.out.println("\n Chat P2P iniciado. Escuchando en el puerto " + puertoEscuchaPropio);
            System.out.println("   IP Destino: " + ipInterlocutor.getHostAddress() + ":" + puertoDestino);
            System.out.println("   Puedes escribir mensajes. Escribe '/salir' para terminar.");
            System.out.println("------------------------------------------------");

            
            
            while (true) {
                
                System.out.print("[" + miNombre + "] >> ");
                String mensajeSalida = scanner.nextLine();
                
                if (mensajeSalida.equalsIgnoreCase(COMANDO_SALIR)) {
                    break;
                }

                byte[] sendData = ("[" + miNombre + "]: " + mensajeSalida).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipInterlocutor, puertoDestino); 
                chatSocket.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                try {
                    chatSocket.setSoTimeout(500); 
                    chatSocket.receive(receivePacket);
                    
                    String mensajeEntrada = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("\r[RECEPTOR] << " + mensajeEntrada); 
                } catch (SocketTimeoutException e) {
                }
            }

        } catch (IllegalArgumentException e) {
            System.err.println(" Error de configuración: " + e.getMessage());
        } catch (BindException e) {
             System.err.println(" Error: Address already in use: El puerto " + puertoEscuchaPropio + " ya está ocupado.");
        } catch (Exception e) {
            System.err.println(" Error en el chat P2P: " + e.getMessage());
        } finally {
            System.out.println("\n Chat P2P finalizado.");
            if (chatSocket != null && !chatSocket.isClosed()) {
                chatSocket.close();
            }
            scanner.close();
        }
    }
}