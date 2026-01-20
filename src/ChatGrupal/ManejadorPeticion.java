/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ChatGrupal;

import ChatAsincrono.MensajeChat;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 *
 * @author anago
 */
public class ManejadorPeticion implements Runnable {

    private DatagramPacket paquete;
    private DatagramSocket socket;

    public ManejadorPeticion(DatagramPacket paquete, DatagramSocket socket) {
        this.paquete = paquete;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            MensajeChat mensaje = MensajeChat.fromPaqueteUDP(paquete);
            if (mensaje == null) {
                return;
            }

            InetSocketAddress remitente
                    = new InetSocketAddress(paquete.getAddress(), paquete.getPort());

            String usuario = mensaje.getEmisor();

            // Registro
            ServidorChatMultihilo.clientes.putIfAbsent(usuario, remitente);

            if (mensaje.getTipo() == MensajeChat.Tipo.BYE) {
                ServidorChatMultihilo.clientes.remove(usuario);
                return;
            }

            // **AÑADE ESTE LOG AQUÍ**
            System.out.println(
                    "Procesando mensaje de " + mensaje.getEmisor()
                    + " en hilo " + Thread.currentThread().getName()
            );
            Thread.sleep(2000);

            for (InetSocketAddress destino : ServidorChatMultihilo.clientes.values()) {
                if (!destino.equals(remitente)) {
                    byte[] datos = mensaje.toTramaBytes();
                    DatagramPacket envio = new DatagramPacket(
                            datos, datos.length,
                            destino.getAddress(), destino.getPort()
                    );
                    socket.send(envio);
                }
            }

        } catch (Exception e) {
            System.out.println("Error en worker: " + e.getMessage());
        }
    }
}
