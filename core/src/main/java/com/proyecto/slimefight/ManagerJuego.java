package com.proyecto.slimefight;

import com.badlogic.gdx.graphics.Color;
import com.proyecto.slimefight.clases.Jugador;
import com.proyecto.slimefight.clases.Punto;

import java.util.ArrayList;

public class ManagerJuego {

    // Diferentes estados en los que pasa la partida
    public enum Estado {
        CUENTA_ATRAS, // 3, 2, 1 Fight
        JUGANDO, // La ronda como tal
        FIN_RONDA, // 3 segs al final de la ronda como para toxiquear
        FIN_PARTIDA // Fin de la partida
    }

    public static final int MAX_JUGADORES = 4;
    public static final int PUNTOS_PARA_GANAR = 15;
    public static final int PUNTOS_MODIFICADORES = 7;
    public static final Punto PUNTOS_VICTORIA = new Punto("Victoria", 2, Color.YELLOW);
    public static final Punto PUNTOS_ELIMINACION = new Punto("Eliminacion", 1, Color.RED);
    private static final float ESPERA_FIN_RONDA = 3f;

    private final ArrayList<Jugador> jugadores = new ArrayList<>();
    private Estado estado = Estado.CUENTA_ATRAS;
    private float timer;
    private Jugador ganadorPartida;
    private boolean cambioDeMapaPendiente;

    public Estado getEstado() {
        return estado;
    }

    public void update(float delta, boolean cuentaAtrasFinalizada) {
        switch (estado) {
            case CUENTA_ATRAS:
                if (cuentaAtrasFinalizada) estado = Estado.JUGANDO;
                break;

            case JUGANDO:
                Jugador vivo = null;
                int vivos = 0;
                for (Jugador j : jugadores) {
                    if (!j.getSlime().isEstaMuerto()) {
                        vivos++;
                        vivo = j;
                    }
                }
                if (vivos <= 1) {
                    if (vivo != null) vivo.sumarPuntos(new Punto(PUNTOS_VICTORIA));
                    estado = Estado.FIN_RONDA;
                    timer = 0;
                }
                break;

            case FIN_RONDA:
                timer += delta;
                if (timer >= ESPERA_FIN_RONDA) {
                    ganadorPartida = buscarGanadorPartida();
                    if (ganadorPartida != null) estado = Estado.FIN_PARTIDA;
                    else {
                        cambioDeMapaPendiente = true;
                        estado = Estado.CUENTA_ATRAS;
                    }
                }
                break;

            case FIN_PARTIDA:
                break;
        }
    }

    public void alMorir(Jugador victima, Jugador asesino) {
        if (estado != Estado.JUGANDO) return;   // no puntuar muertes post-victoria
        if (asesino != null && asesino != victima) asesino.sumarPuntos(new Punto(PUNTOS_ELIMINACION));
    }

    private Jugador buscarGanadorPartida() {
        Jugador mejor = null;
        int puntosMejor = 0;
        boolean empate = false;
        for (Jugador j : jugadores) {
            int puntosTotales = 0;
            for (Punto p : j.getPuntos()) puntosTotales += p.getValor();
            if (puntosTotales < PUNTOS_PARA_GANAR) continue;
            if (mejor == null || puntosTotales > puntosMejor) { mejor = j; puntosMejor = puntosTotales; empate = false; }
            else if (puntosTotales == puntosMejor) empate = true;
        }
        return empate ? null : mejor;   // si empatan arriba, se juega otra ronda
    }

    public boolean consumirCambioDeMapa() {
        boolean r = cambioDeMapaPendiente;
        cambioDeMapaPendiente = false;
        return r;
    }

    public Jugador getJugador(int id) {
        for (Jugador j : jugadores) {
            if (j.getId() == id) return j;
        }
        return null;   // id inválido o -1: sin atacante
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public void eliminarJugador(Jugador jugador) {
        if (jugadores.contains(jugador)) {
            jugadores.remove(jugador);
            return;
        }
        System.out.println("Jugador no encontrado.");
    }

    public void añadirJugador(Jugador jugador) {
        if (jugadores.size() < MAX_JUGADORES) {
            this.jugadores.add(jugador);
            return;
        }
        System.out.println("Lista al maximo.");
    }
}
