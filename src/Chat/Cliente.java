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
import java.util.Scanner;
import java.util.regex.*;

public class Cliente {

    private static final int PUERTO = 9876;

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        System.out.print("Introduce tu nombre: ");
        String nombre = sc.nextLine();

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(5000); // ⏱️ Timeout UDP

        InetAddress direccionServidor = InetAddress.getByName("localhost");

        String mensaje = "@hola#" + nombre + "@";
        byte[] bufferEnvio = mensaje.getBytes();

        DatagramPacket paquete =
                new DatagramPacket(bufferEnvio, bufferEnvio.length,
                        direccionServidor, PUERTO);

        socket.send(paquete);

        byte[] bufferRecepcion = new byte[1024];
        DatagramPacket respuesta = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);

        try {
            socket.receive(respuesta);
            String mensajeRespuesta = new String(respuesta.getData(), 0, respuesta.getLength());

            Pattern pattern = Pattern.compile("@hola#(.+?)@");
            Matcher matcher = pattern.matcher(mensajeRespuesta);

            if (matcher.matches()) {
                String nombreServidor = matcher.group(1);
                System.out.println("Conectado al servidor: " + nombreServidor);
            } else {
                System.out.println("Respuesta con formato incorrecto");
            }

        } catch (SocketTimeoutException e) {
            System.out.println(" El servidor no respondió (UDP)");
        }

        socket.close();
    }
}