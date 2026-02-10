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
import java.util.Scanner;

/**
 *
 * @author anago
 */
public class ClienteMiniFTP {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Servidor (IP): ");
        String ip = sc.nextLine();
        System.out.print("Puerto: ");
        int puerto = Integer.parseInt(sc.nextLine());

        Socket socket = new Socket(ip, puerto);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

        System.out.println(in.readLine()); // mensaje bienvenida

        while (true) {
            System.out.print("miniftp> ");
            String cmd = sc.nextLine();
            if (cmd.equals("exit") || cmd.equals("quit")) break;

            if (cmd.startsWith("put ")) {
                File f = new File(cmd.substring(4).trim());
                if (!f.exists()) {
                    System.out.println("Fichero no existe");
                    continue;
                }
                out.write(cmd + "\n");
                out.write(f.length() + "\n");
                out.flush();

                FileInputStream fis = new FileInputStream(f);
                byte[] buffer = new byte[4096];
                int leido;
                while ((leido = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, leido);
                }
                bos.flush();
                fis.close();
                System.out.println(in.readLine());
            } else if (cmd.startsWith("get ")) {
                out.write(cmd + "\n");
                out.flush();
                String respuesta = in.readLine();
                if (respuesta.startsWith("ERR")) {
                    System.out.println(respuesta);
                    continue;
                }
                String[] parts = respuesta.split(" ");
                long tam = Long.parseLong(parts[1]);
                File f = new File(cmd.substring(4).trim());
                FileOutputStream fos = new FileOutputStream(f);
                long recibidos = 0;
                int leido;
                byte[] buffer = new byte[4096];
                while (recibidos < tam && (leido = bis.read(buffer, 0, (int)Math.min(buffer.length, tam - recibidos))) != -1) {
                    fos.write(buffer, 0, leido);
                    recibidos += leido;
                }
                fos.close();
                System.out.println("Fichero recibido: " + f.getName());
            } else {
                out.write(cmd + "\n");
                out.flush();
                String linea;
                while ((linea = in.readLine()) != null) {
                    if (linea.isEmpty()) break; // fin de lista en ls
                    System.out.println(linea);
                    if (!cmd.equals("ls")) break; // solo una línea si no es ls
                }
            }
        }

        socket.close();
        sc.close();
    }
}