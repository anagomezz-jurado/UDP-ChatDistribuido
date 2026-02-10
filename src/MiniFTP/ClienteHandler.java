/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package MiniFTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 *
 * @author anago
 */
public class ClienteHandler implements Runnable {

    private Socket socket;
    private File carpetaActual;
    private String usuario;

    public ClienteHandler(Socket socket, File carpetaRaiz) {
        this.socket = socket;
        this.carpetaActual = carpetaRaiz;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ) {
            out.write("Bienvenido al MiniFTP\n");
            out.flush();

            String linea;
            while ((linea = in.readLine()) != null) {
                if (linea.startsWith("hola")) {
                    usuario = linea.substring(5).trim();
                    out.write("OK Hola " + usuario + "\n");
                    out.flush();
                } else if (linea.equals("adios")) {
                    out.write("OK Hasta luego " + usuario + "\n");
                    out.flush();
                    break;
                } else if (linea.equals("ls")) {
                    for (File f : carpetaActual.listFiles()) {
                        out.write(f.getName() + "\n");
                    }
                    out.write("\n"); // línea vacía indica fin
                    out.flush();
                } else if (linea.startsWith("get ")) {
                    String nombreFichero = linea.substring(4).trim();
                    File fichero = new File(carpetaActual, nombreFichero);
                    if (!fichero.exists()) {
                        out.write("ERR Fichero no encontrado\n");
                        out.flush();
                        continue;
                    }
                    long tam = fichero.length();
                    out.write("OK " + tam + "\n");
                    out.flush();

                    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                    FileInputStream fis = new FileInputStream(fichero);
                    byte[] buffer = new byte[4096];
                    int leido;
                    while ((leido = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, leido);
                    }
                    bos.flush();
                    fis.close();
                } else if (linea.startsWith("put ")) {
                    String nombreFichero = linea.substring(4).trim();
                    String tamStr = in.readLine();
                    long tam = Long.parseLong(tamStr);

                    File fichero = new File(carpetaActual, nombreFichero);
                    FileOutputStream fos = new FileOutputStream(fichero);
                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    byte[] buffer = new byte[4096];
                    long recibidos = 0;
                    int leido;
                    while (recibidos < tam && (leido = bis.read(buffer, 0, (int)Math.min(buffer.length, tam - recibidos))) != -1) {
                        fos.write(buffer, 0, leido);
                        recibidos += leido;
                    }
                    fos.close();
                    out.write("OK Fichero recibido\n");
                    out.flush();
                } else if (linea.equals("pwd")) {
                    out.write(carpetaActual.getAbsolutePath() + "\n");
                    out.flush();
                } else if (linea.startsWith("cd ")) {
                    String nuevo = linea.substring(3).trim();
                    File nuevaCarpeta = new File(carpetaActual, nuevo);
                    if (nuevaCarpeta.exists() && nuevaCarpeta.isDirectory()) {
                        carpetaActual = nuevaCarpeta;
                        out.write("OK Cambio de carpeta a " + carpetaActual.getAbsolutePath() + "\n");
                    } else {
                        out.write("ERR Carpeta no válida\n");
                    }
                    out.flush();
                } else {
                    out.write("ERR Comando no reconocido\n");
                    out.flush();
                }
            }

            socket.close();
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        }
    }
}