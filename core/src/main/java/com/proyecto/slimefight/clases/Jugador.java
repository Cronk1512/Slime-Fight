
package com.proyecto.slimefight.clases;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.proyecto.slimefight.actores.Slime;

import java.util.ArrayList;

public class Jugador {
    // TODO en el futuro debe contener su hilo cliente o algo asi ns

    // Opciones de personalizacion disponibles (cambialas si agregas assets: ojos/ojosN.png, bocas/bocaN.png)
    public static final int CANT_OJOS = 3;
    public static final int CANT_BOCAS = 3;
    public static final Color[] PALETA = {
        Color.valueOf("D94050"), // rojo
        Color.valueOf("4058D9"), // azul
        Color.valueOf("9ACD32"), // verde
        Color.valueOf("F2C230"), // amarillo
        Color.valueOf("F28A30"), // naranja
        Color.valueOf("9B59D9"), // violeta
        Color.valueOf("F27AB0"), // rosa
        Color.valueOf("87CEEB"), // celeste
        Color.valueOf("FFFFFF"), // blanco
    };

    private final int id;
    private String nombre;
    private ArrayList<Punto> puntos;

    private Sprite ojosSlime;
    private Sprite bocaSlime;
    private Color colorSlime;

    // Aspecto elegido en el lobby. Son indices (lo unico que se puede mandar por red);
    // los Sprites se arman a partir de ellos en la pantalla que tenga las texturas.
    private int colorIdx = 0;
    private int ojosIdx = 0;
    private int bocaIdx = 0;
    private boolean listo = false;

    private Slime slime;

    public Jugador(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        puntos = new ArrayList<>();
        colorSlime = PALETA[0];
    }

    public void sumarPuntos(Punto p) {
        puntos.add(p);
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public ArrayList<Punto> getPuntos() { return puntos; }
    public Slime getSlime() { return slime; }
    public void setSlime(Slime s) { this.slime = s; }

    public int getTotalPuntos() {
        int total = 0;
        for (Punto p : puntos) total += p.getValor();
        return total;
    }

    public Sprite getOjosSlime() {
        return ojosSlime;
    }

    public void setOjosSlime(Sprite ojosSlime) {
        this.ojosSlime = ojosSlime;
    }

    public Sprite getBocaSlime() {
        return bocaSlime;
    }

    public void setBocaSlime(Sprite bocaSlime) {
        this.bocaSlime = bocaSlime;
    }

    public Color getColorSlime() {
        return colorSlime;
    }

    public void setColorSlime(Color colorSlime) {
        this.colorSlime = colorSlime;
    }

    // ---- Aspecto elegido (lobby)

    public int getColorIdx() { return colorIdx; }

    public void setColorIdx(int colorIdx) {
        this.colorIdx = colorIdx;
        this.colorSlime = PALETA[colorIdx];
    }

    public int getOjosIdx() { return ojosIdx; }
    public void setOjosIdx(int ojosIdx) { this.ojosIdx = ojosIdx; }

    public int getBocaIdx() { return bocaIdx; }
    public void setBocaIdx(int bocaIdx) { this.bocaIdx = bocaIdx; }

    public boolean isListo() { return listo; }
    public void setListo(boolean listo) { this.listo = listo; }

    /** Rutas relativas a assets, para usar con Utils.obtenerRutaInterna(...) */
    public String getRutaOjos() { return "ojos/ojos" + (ojosIdx + 1) + ".png"; }
    public String getRutaBoca() { return "bocas/boca" + (bocaIdx + 1) + ".png"; }
}
