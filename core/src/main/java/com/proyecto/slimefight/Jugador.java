package com.proyecto.slimefight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.proyecto.slimefight.actores.Slime;

public class Jugador {
    // TODO en el futuro debe contener su hilo cliente o algo asi ns

    private final int id;
    private final String nombre;
    private int puntos = 0;

    private Sprite ojosSlime;
    private Sprite bocaSlime;
    private Color colorSlime;

    private Slime slime;

    public Jugador(int id, String nombre) { this.id = id; this.nombre = nombre; }

    public void sumarPuntos(int p) { puntos += p; }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getPuntos() { return puntos; }
    public Slime getSlime() { return slime; }
    public void setSlime(Slime s) { this.slime = s; }

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
}
