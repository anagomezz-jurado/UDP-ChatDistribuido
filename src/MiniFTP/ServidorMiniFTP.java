/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package MiniFTP;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author anago
 */
public class ServidorMiniFTP {

    private static final int PUERTO = 2121; // puerto TCP del servidor
    private static File carpetaRaiz ;

    public static void main(String[] args) throws Exception {
        
        if (args.length < 1) {
            System.out.println("Uso: java ServidorMiniFTP <carpeta_raiz>");
            return;
        }

        carpetaRaiz = new File(args[0]);
        if (!carpetaRaiz.exists() || !carpetaRaiz.isDirectory()) {
            System.out.println("Carpeta no válida.");
            return;
        }

        ServerSocket serverSocket = new ServerSocket(PUERTO);
        System.out.println("Servidor MiniFTP iniciado en puerto " + PUERTO);

        ExecutorService pool = Executors.newCachedThreadPool();

        while (true) {
            Socket cliente = serverSocket.accept();
            pool.execute(new ClienteHandler(cliente, carpetaRaiz));
        }
    }
}