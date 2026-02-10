/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto_repositorio_TCP_UDP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author anago
 */
public class ClienteHibridoM {

    public static final String HOST = "localhost";
    public static final int PUERTO_UDP = 5000;
    public static final int PUERTO_TCP = 6000;

    private DatagramSocket udpSocket;
    private Socket tcpSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private volatile boolean activo = true;

    public static void main(String[] args) {
        new ClienteHibridoM().ejecuta();
    }

    public void ejecuta() {
        try (Scanner teclado = new Scanner(System.in)) {

            // ===== CONEXIÓN UDP =====
            udpSocket = new DatagramSocket();
            InetSocketAddress servidorUDP
                    = new InetSocketAddress(HOST, PUERTO_UDP);

            System.out.print("Tu nombre: ");
            String nombre = teclado.nextLine().trim();

            // Login por UDP
            enviarUDP("@login#" + nombre, servidorUDP);

            // ===== CONEXIÓN TCP =====
            tcpSocket = new Socket(HOST, PUERTO_TCP);
            dis = new DataInputStream(tcpSocket.getInputStream());
            dos = new DataOutputStream(tcpSocket.getOutputStream());

            // ===== HILO RECEPTOR UDP =====
            Thread hiloUDP = new Thread(new ReceptorUDP(udpSocket));
            hiloUDP.setDaemon(true);
            hiloUDP.start();

            System.out.println("\n=== CLIENTE HÍBRIDO ===");
            mostrarAyuda();

            // ===== BUCLE PRINCIPAL =====
            while (activo) {
                System.out.print("> ");
                String linea = teclado.nextLine().trim();
                if (linea.isEmpty()) {
                    continue;
                }

                // Comandos UDP
                if (linea.startsWith("@") || linea.equals(".")) {
                    enviarUDP(linea, servidorUDP);
                    if (linea.equals(".")) {
                        activo = false;
                    }
                } // Comandos TCP
                else {
                    procesarTCP(linea, servidorUDP);
                }
            }

            udpSocket.close();
            tcpSocket.close();
            System.out.println("Desconectado.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Procesa comandos TCP
     */
    private void procesarTCP(String linea,
            InetSocketAddress servidorUDP) throws IOException {

        String[] partes = linea.split(" ");
        String comando = partes[0].toUpperCase();

        switch (comando) {

            case "SUBIR":
                if (partes.length >= 2) {
                    subirFichero(partes[1]);
                } else {
                    System.out.println("Uso: SUBIR fichero");
                }
                break;

            case "BAJAR":
                if (partes.length >= 2) {
                    bajarFichero(partes[1]);
                } else {
                    System.out.println("Uso: BAJAR fichero");
                }
                break;

            case "LISTAR":
                dos.writeUTF("LISTAR");
                System.out.println(dis.readUTF());
                break;

            case "SALIR":
                enviarUDP(".", servidorUDP);
                dos.writeUTF("SALIR");
                System.out.println(dis.readUTF());
                activo = false;
                break;

            case "AYUDA":
                mostrarAyuda();
                break;

            default:
                enviarUDP(linea, servidorUDP);
        }
    }

    /**
     * SUBIR fichero al servidor (TCP)
     */
    private void subirFichero(String nombreFichero) throws IOException {

        // ️⃣ Comprobar existencia
        File fichero = new File(nombreFichero);
        if (!fichero.exists() || !fichero.isFile()) {
            System.out.println("Error: fichero no existe");
            return;
        }

        // 2️⃣ Enviar comando
        dos.writeUTF("SUBIR " + nombreFichero);

        // 3️⃣ Esperar OK
        String respuesta = dis.readUTF();
        if (!respuesta.startsWith("OK")) {
            System.out.println("Error servidor: " + respuesta);
            return;
        }

        // 4️⃣ Leer fichero
        byte[] contenido;
        try (FileInputStream fis = new FileInputStream(fichero)) {
            contenido = fis.readAllBytes();
        }

        // 5️⃣ Enviar tamaño
        dos.writeInt(contenido.length);

        // 6️⃣ Enviar bytes
        dos.write(contenido);
        dos.flush();

        // 7️⃣ Confirmación
        System.out.println(dis.readUTF());
    }

    /**
     * BAJAR fichero (YA DADO)
     */
    private void bajarFichero(String nombreFichero) throws IOException {

        dos.writeUTF("BAJAR " + nombreFichero);

        String respuesta = dis.readUTF();
        if (!respuesta.startsWith("OK")) {
            System.out.println("Error: " + respuesta);
            return;
        }

        int tamaño = dis.readInt();
        byte[] contenido = new byte[tamaño];
        dis.readFully(contenido);

        try (FileOutputStream fos = new FileOutputStream(nombreFichero)) {
            fos.write(contenido);
        }

        System.out.println("Guardado: " + nombreFichero);
    }

    /**
     * Enviar mensaje UDP
     */
    private void enviarUDP(String mensaje,
            InetSocketAddress destino) throws IOException {

        byte[] datos = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(
                datos, datos.length,
                destino.getAddress(), destino.getPort()
        );
        udpSocket.send(paquete);
    }

    private void mostrarAyuda() {
        System.out.println("\n--- UDP ---");
        System.out.println("@quien   | lista usuarios");
        System.out.println("mensaje  | chat");
        System.out.println(".        | salir");

        System.out.println("\n--- TCP ---");
        System.out.println("LISTAR");
        System.out.println("SUBIR fichero");
        System.out.println("BAJAR fichero");
        System.out.println("SALIR");
    }
}

/**
 * Hilo receptor UDP
 */
class ReceptorUDP implements Runnable {

    private DatagramSocket socket;

    public ReceptorUDP(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket paquete
                        = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                String mensaje = new String(
                        paquete.getData(), 0, paquete.getLength());

                System.out.println("\n[MSG] " + mensaje);
                System.out.print("> ");

            } catch (IOException e) {
                break;
            }
        }
    }
}
