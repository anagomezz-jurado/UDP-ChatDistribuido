/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ChatGrupal;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author anago
 */

public class ServidorChatMultihilo {

    private static final int PUERTO = 9876;

    static Map<String, InetSocketAddress> clientes = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(PUERTO);
        System.out.println("Servidor Chat Multihilo iniciado en puerto " + PUERTO);

        while (true) {
            DatagramPacket paquete = new DatagramPacket(new byte[1024], 1024);
            socket.receive(paquete);

            new Thread(new ManejadorPeticion(paquete, socket)).start();
        }
    }
}