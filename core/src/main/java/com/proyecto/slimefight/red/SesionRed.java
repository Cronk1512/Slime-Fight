
package com.proyecto.slimefight.red;

import com.proyecto.slimefight.clases.Jugador;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class SesionRed {
    private SesionRed() { }

    public static RedUDP red;
    public static boolean esHost;
    public static int idLocal;
    public static InetSocketAddress servidor;                                          // solo cliente
    public static final Map<Integer, Jugador> jugadores = new TreeMap<>();             // id -> jugador
    public static final Map<Integer, InetSocketAddress> direcciones = new HashMap<>(); // id -> addr (solo host)

    public static boolean activa() {
        return red != null && red.estaAbierta();
    }

    public static void cerrar() {
        if (red != null) red.cerrar();
        red = null;
        servidor = null;
        jugadores.clear();
        direcciones.clear();
    }
}
