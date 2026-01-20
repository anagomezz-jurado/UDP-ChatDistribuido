/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ChatGrupal;

import ChatAsincrono.MensajeChat;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author anago
 */
public class ServidorChat {

    private static final int PUERTO = 9876;

    // nombreUsuario -> direccion (IP + puerto)
    private static Map<String, InetSocketAddress> clientes = new HashMap<>();

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(PUERTO);
        System.out.println("Servidor Chat Grupal iniciado en puerto " + PUERTO);

        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                MensajeChat mensaje = MensajeChat.fromPaqueteUDP(paquete);
                if (mensaje == null) {
                    System.out.println("Trama inválida recibida, ignorada.");
                    continue;
                }

                InetSocketAddress direccionRemitente =
                        new InetSocketAddress(paquete.getAddress(), paquete.getPort());

                String usuario = mensaje.getEmisor();

                // Registrar cliente si no existe
                clientes.putIfAbsent(usuario, direccionRemitente);

                if (mensaje.getTipo() == MensajeChat.Tipo.BYE) {
                    clientes.remove(usuario);
                    System.out.println("Usuario desconectado: " + usuario);
                    continue;
                }

                System.out.println("Mensaje de " + usuario + ": " + mensaje.getContenido());

                // Reenvío a todos menos al emisor
                for (Map.Entry<String, InetSocketAddress> entry : clientes.entrySet()) {
                    InetSocketAddress destino = entry.getValue();

                    if (!destino.equals(direccionRemitente)) {
                        byte[] datos = mensaje.toTramaBytes();
                        DatagramPacket envio = new DatagramPacket(
                                datos, datos.length,
                                destino.getAddress(), destino.getPort()
                        );
                        socket.send(envio);
                    }
                }

            } catch (Exception e) {
                System.out.println("Error procesando paquete, seguimos...");
            }
        }
    }
}