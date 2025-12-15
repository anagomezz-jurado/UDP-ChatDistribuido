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
                
                String mensaje = new String(paquete.getData(), 0, paquete.getLength());
              
                System.out.println("\r[RECEPTOR] << " + mensaje); 
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