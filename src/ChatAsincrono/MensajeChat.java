/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ChatAsincrono;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author anago
 */

public class MensajeChat {
    
    public enum Tipo {
        HELLO,   
        TEXT,    
        BYE    
    }
    
    private Tipo tipo;
    private String emisor;
    private String contenido;

    // Constructor 
    public MensajeChat(Tipo tipo, String emisor, String contenido) {
        this.tipo = tipo;
        this.emisor = emisor;
        this.contenido = (contenido != null) ? contenido : ""; 
    }

    public byte[] toTramaBytes() {
        String trama = tipo.name() + "|" + emisor + "|" + contenido;
        return trama.getBytes(StandardCharsets.UTF_8);
    }
    
    public static MensajeChat fromPaqueteUDP(DatagramPacket paquete) {
        String trama = new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8);
        String[] partes = trama.split("\\|", 3); // Dividir por '|' en máximo 3 partes

        if (partes.length < 2) {
            return null; 
        }

        try {
            Tipo tipo = Tipo.valueOf(partes[0].toUpperCase());
            String emisor = partes[1];
            String contenido = (partes.length == 3) ? partes[2] : ""; 
            
            return new MensajeChat(tipo, emisor, contenido);

        } catch (IllegalArgumentException e) {
            return null; 
        }
    }
    
    // Getters
    public Tipo getTipo() { return tipo; }
    public String getEmisor() { return emisor; }
    public String getContenido() { return contenido; }
}