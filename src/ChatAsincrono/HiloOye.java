/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ChatAsincrono;

/**
 *
 * @author anago
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class HiloOye implements Runnable {
    
    private DatagramSocket socket;
    private volatile boolean activo = true; 

    public HiloOye(DatagramSocket socket) throws SocketException {
        this.socket = socket;
        this.socket.setSoTimeout(0); 
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        
        while (activo) {
            try {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete); 
                
                MensajeChat mensajeRecibido = MensajeChat.fromPaqueteUDP(paquete);

                if (mensajeRecibido != null) {
                    if (mensajeRecibido.getTipo() == MensajeChat.Tipo.TEXT) {
                        System.out.println("\r[" + mensajeRecibido.getEmisor() + "] << " + mensajeRecibido.getContenido()); 
                    } else if (mensajeRecibido.getTipo() == MensajeChat.Tipo.BYE) {
                        System.out.println("\r*** " + mensajeRecibido.getEmisor() + " se ha desconectado. ***");
                    }
                } else {
                    System.out.println("\r[AVISO] << Trama de protocolo no válida recibida."); 
                }
                
                System.out.print("[Hablando] >> "); 
                
            } catch (SocketException e) {
                if (activo) {
                    System.err.println("Error de socket en recepción: " + e.getMessage());
                }
                break;
            } catch (Exception e) {
                System.err.println("Error en el Hilo Oye: " + e.getMessage());
            }
        }
        System.out.println("Hilo Oye (Receptor) terminado.");
    }
    
    public void detener() {
        activo = false;
    }
}