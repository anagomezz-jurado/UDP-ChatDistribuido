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
        
        //Lectura del puerto
        if (args.length > 0) {
            try {
                PUERTO_ESCUCHA = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Usando puerto por defecto " + PUERTO_ESCUCHA);
            }
        }
        
        Scanner sc = new Scanner(System.in);
        DatagramSocket chatSocket = null;
        HiloOye receptor = null; 

        System.out.print(" Tu Nombre de Usuario: ");
        String miNombre = sc.nextLine();
        
        System.out.print(" IP del Interlocutor (ej. localhost): ");
        String ipInterlocutorStr = sc.nextLine();
        
        System.out.print("Puerto del Interlocutor (DESTINO, ej. 9876): "); 
        int puertoInterlocutor = sc.nextInt();
        sc.nextLine(); 

        try {
            //Configuración de parámetros de conexión
            InetAddress ipInterlocutor = InetAddress.getByName(ipInterlocutorStr);
            
            chatSocket = new DatagramSocket(PUERTO_ESCUCHA);
            
            //Aranco el hilo receptor
            receptor = new HiloOye(chatSocket);
            Thread hiloReceptor = new Thread(receptor);
            hiloReceptor.start(); 
            
            System.out.println("\n Chat Asíncrono iniciado. Escuchando en " + PUERTO_ESCUCHA);
            System.out.println("------------------------------------------------");

            
            //Comienzo con el bucle de la conversación
            while (true) {
                System.out.print("[Hablando] >> ");
                String mensajeSalida = sc.nextLine(); 
                
                if (mensajeSalida.equalsIgnoreCase(COMANDO_SALIR)) {
                    MensajeChat bye = new MensajeChat(MensajeChat.Tipo.BYE, miNombre, null);
                    byte[] sendData = bye.toTramaBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipInterlocutor, puertoInterlocutor);
                    chatSocket.send(sendPacket);
                    break;
                }

                //Envío el mensaje TEXT
                MensajeChat mensajeChat = new MensajeChat(MensajeChat.Tipo.TEXT, miNombre, mensajeSalida);
                
                byte[] sendData = mensajeChat.toTramaBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipInterlocutor, puertoInterlocutor);
                chatSocket.send(sendPacket);
            }

        } catch (Exception e) {
            System.err.println("Error en el chat P2P: " + e.getMessage());
        } finally {
            System.out.println("\nChat P2P finalizado. Cerrando recursos...");
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