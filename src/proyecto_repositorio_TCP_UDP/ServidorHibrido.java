/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto_repositorio_TCP_UDP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 *
 * @author anago
 */
//Del servidor híbrido sólo puede haber una instancia: lo hago todo estático
public class ServidorHibrido {

    public static final int PUERTO_UDP = 5000;
    public static final int PUERTO_TCP = 6000;
    public static final String DIRECTORIO = "repositorio";

    private static DatagramSocket udpSocket;

    public static void main(String[] args) {

        new File(DIRECTORIO).mkdirs();

        try {
            // UDP
            udpSocket = new DatagramSocket(PUERTO_UDP);
            new Thread(new HiloUDP(udpSocket)).start();
            System.out.println("[SERVIDOR] UDP OK");

            // TCP
            ServerSocket tcp = new ServerSocket(PUERTO_TCP);
            System.out.println("[SERVIDOR] TCP OK");

            while (true) {
                Socket cliente = tcp.accept();
                new Thread(new HiloTCP(cliente)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Enviar UDP a uno
    public static void enviarUDP(String msg,
                                 InetSocketAddress dest) throws IOException {

        byte[] datos = msg.getBytes();
        DatagramPacket p = new DatagramPacket(
                datos, datos.length,
                dest.getAddress(), dest.getPort());

        synchronized (udpSocket) {
            udpSocket.send(p);
        }
    }

    // Broadcast UDP
    public static void broadcastUDP(String msg,
                                    String excluir) throws IOException {

        for (Map.Entry<String, InetSocketAddress> e :
                Estructuras.getUsuariosCopia().entrySet()) {

            if (!e.getKey().equals(excluir))
                enviarUDP(msg, e.getValue());
        }
    }
}

/**
 * Hilo UDP
 */
class HiloUDP implements Runnable {

    private DatagramSocket socket;

    public HiloUDP(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket p =
                        new DatagramPacket(buffer, buffer.length);
                socket.receive(p);

                String msg = new String(
                        p.getData(), 0, p.getLength());

                InetSocketAddress dir =
                        new InetSocketAddress(p.getAddress(), p.getPort());

                procesarMensaje(msg, dir);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void procesarMensaje(String mensaje,
                                 InetSocketAddress dir) throws IOException {

        if (mensaje.startsWith("@login#")) {
            String nombre = mensaje.substring(7);
            Estructuras.addUsuario(nombre, dir);
            ServidorHibrido.enviarUDP("OK Bienvenido " + nombre, dir);
            ServidorHibrido.broadcastUDP(
                    ">>> " + nombre + " conectado", nombre);
        }
        else if (mensaje.equals(".")) {
            String nombre = Estructuras.buscarPorDireccion(dir);
            if (nombre != null) {
                Estructuras.removeUsuario(nombre);
                ServidorHibrido.broadcastUDP(
                        ">>> " + nombre + " desconectado", nombre);
            }
        }
        else if (mensaje.equals("@quien")) {
            ServidorHibrido.enviarUDP(
                    Estructuras.getListaUsuarios(), dir);
        }
        else {
            String nombre = Estructuras.buscarPorDireccion(dir);
            if (nombre == null) nombre = "Desconocido";
            ServidorHibrido.broadcastUDP(
                    nombre + ": " + mensaje, nombre);
        }
    }
}

/**
 * Hilo TCP
 */
class HiloTCP implements Runnable {

    private Socket socket;

    public HiloTCP(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (DataInputStream dis =
                     new DataInputStream(socket.getInputStream());
             DataOutputStream dos =
                     new DataOutputStream(socket.getOutputStream())) {

            while (true) {
                String linea;
                try {
                    linea = dis.readUTF();
                } catch (EOFException e) {
                    break;
                }

                String[] p = linea.split(" ");
                switch (p[0].toUpperCase()) {

                    case "LISTAR":
                        listar(dos);
                        break;

                    case "SUBIR":
                        subir(dis, dos, p[1]);
                        break;

                    case "BAJAR":
                        bajar(dos, p[1]);
                        break;

                    case "SALIR":
                        dos.writeUTF("OK Adios");
                        return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listar(DataOutputStream dos) throws IOException {

        File[] f = new File(
                ServidorHibrido.DIRECTORIO).listFiles();

        StringBuilder sb =
                new StringBuilder("Ficheros:\n");

        if (f != null)
            for (File x : f)
                sb.append(" - ").append(x.getName()).append("\n");

        dos.writeUTF(sb.toString());
    }

    private void subir(DataInputStream dis,
                       DataOutputStream dos,
                       String nombre) throws IOException {

        dos.writeUTF("OK");

        int tam = dis.readInt();
        byte[] datos = new byte[tam];
        dis.readFully(datos);

        try (FileOutputStream fos =
                     new FileOutputStream(
                             ServidorHibrido.DIRECTORIO + "/" + nombre)) {
            fos.write(datos);
        }

        dos.writeUTF("OK Recibido");
        ServidorHibrido.broadcastUDP(
                ">>> Subido fichero: " + nombre, null);
    }

    private void bajar(DataOutputStream dos,
                       String nombre) throws IOException {

        File f = new File(
                ServidorHibrido.DIRECTORIO, nombre);

        if (!f.exists()) {
            dos.writeUTF("ERR No existe");
            return;
        }

        byte[] datos;
        try (FileInputStream fis = new FileInputStream(f)) {
            datos = fis.readAllBytes();
        }

        dos.writeUTF("OK");
        dos.writeInt(datos.length);
        dos.write(datos);
        dos.flush();
    }
}