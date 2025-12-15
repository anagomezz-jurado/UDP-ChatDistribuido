/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ChatAsincrono;

/**
 *
 * @author anago
 */

import java.net.*;
import java.util.Scanner;

public class ChatAsincrono {
    
    private static int PUERTO_ESCUCHA = 9876;
    private static final String COMANDO_SALIR = "/salir";

    public static void main(String[] args) throws Exception {
    
        if (args.length > 0) {
            try {
                // Si se pasa un argumento, lo usamos como puerto de escucha
                PUERTO_ESCUCHA = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("🔴 Error: El primer argumento debe ser un número de puerto válido.");
                return; // Termina el programa si el argumento no es un número
            }
        }
        
        Scanner sc = new Scanner(System.in);
        DatagramSocket chatSocket = null;
        HiloOye receptor = null; 

        System.out.print("💬 Tu Nombre de Usuario: ");
        String miNombre = sc.nextLine();
        
        // El resto de la información (IP y puerto del interlocutor) se sigue pidiendo por consola
        System.out.print("💻 IP del Interlocutor (ej. localhost): ");
        String ipInterlocutorStr = sc.nextLine();
        
        System.out.print("🚪 Puerto del Interlocutor (DESTINO, ej. 9876): "); 
        int puertoInterlocutor = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea

        try {
            InetAddress ipInterlocutor = InetAddress.getByName(ipInterlocutorStr);
            
            // Abrimos el socket para escuchar en el puerto PUERTO_ESCUCHA (tomado del argumento o 9876)
            chatSocket = new DatagramSocket(PUERTO_ESCUCHA);
            
            // 1. ARRANCAR EL HILO RECEPTOR ("Oye")
            receptor = new HiloOye(chatSocket);
            Thread hiloReceptor = new Thread(receptor);
            hiloReceptor.start(); 
            
            System.out.println("\n✅ Chat Asíncrono iniciado. Escuchando en el puerto " + PUERTO_ESCUCHA);
            System.out.println("------------------------------------------------");

            // 2. BUCLE PRINCIPAL (Hilo "Habla")
            while (true) {
                System.out.print("[Hablando] >> ");
                String mensajeSalida = sc.nextLine(); 
                
                if (mensajeSalida.equalsIgnoreCase(COMANDO_SALIR)) {
                    break;
                }

                // Usamos el formato simple de la Fase 4 para el envío
                String tramaEnvio = "[" + miNombre + "]: " + mensajeSalida;
                byte[] sendData = tramaEnvio.getBytes();
                
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipInterlocutor, puertoInterlocutor);
                chatSocket.send(sendPacket);
            }

        } catch (Exception e) {
            System.err.println("🔴 Error en el chat P2P: " + e.getMessage());
        } finally {
            System.out.println("\n👋 Chat P2P finalizado. Cerrando recursos...");
            if (receptor != null) {
                receptor.detener(); 
            }
            if (chatSocket != null) {
                chatSocket.close(); 
            }
            sc.close();
        }
    }
}