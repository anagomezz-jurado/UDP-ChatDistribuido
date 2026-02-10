/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto_repositorio_TCP_UDP;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author anago
 */
public class Estructuras {

    // Mapa compartido: nombre → dirección UDP
    private static Map<String, InetSocketAddress> usuarios = new HashMap<>();

    // Añadir usuario
    public static synchronized void addUsuario(String nombre, InetSocketAddress direccion) {
        usuarios.put(nombre, direccion);
    }

    // Eliminar usuario
    public static synchronized void removeUsuario(String nombre) {
        usuarios.remove(nombre);
    }

    // Devolver COPIA del mapa
    public static synchronized Map<String, InetSocketAddress> getUsuariosCopia() {
        return new HashMap<>(usuarios);
    }

    // Buscar nombre a partir de IP:puerto
    public static synchronized String buscarPorDireccion(InetSocketAddress direccion) {
        for (Map.Entry<String, InetSocketAddress> entry : usuarios.entrySet()) {
            if (entry.getValue().equals(direccion)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ===================== YA IMPLEMENTADOS =====================

    public static synchronized String getListaUsuarios() {
        if (usuarios.isEmpty()) {
            return "Nadie conectado";
        }
        return String.join(", ", usuarios.keySet());
    }

    public static synchronized boolean hayUsuarios() {
        return !usuarios.isEmpty();
    }

    public static synchronized InetSocketAddress getDireccion(String nombre) {
        return usuarios.get(nombre);
    }
}
