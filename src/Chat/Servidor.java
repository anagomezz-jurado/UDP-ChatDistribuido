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
import java.util.regex.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Servidor {
    private static final int PUERTO = 9876;
    private static final String NOMBRE_SERVIDOR = "SERVIDOR_UDP";

     public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(PUERTO);
        System.out.println("Servidor escuchando en puerto " + PUERTO);

        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                System.out.println("Recibido: " + mensaje);

                Pattern pattern = Pattern.compile("@hola#(.+?)@");
                Matcher matcher = pattern.matcher(mensaje);

                if (matcher.matches()) {
                    String nombreCliente = matcher.group(1);
                    System.out.println("Saludo correcto de: " + nombreCliente);

                    String respuesta = "@hola#" + NOMBRE_SERVIDOR + "@";
                    byte[] datosRespuesta = respuesta.getBytes();

                    DatagramPacket paqueteRespuesta =
                            new DatagramPacket(datosRespuesta, datosRespuesta.length,
                                    paquete.getAddress(), paquete.getPort());

                    socket.send(paqueteRespuesta);

                } else {
                    System.out.println("⚠ Trama incorrecta, ignorada");
                }

            } catch (Exception e) {
                // UDP NUNCA debe morir por un paquete malformado
                System.out.println("Error procesando paquete, continuamos...");
            }
        }
    }
}