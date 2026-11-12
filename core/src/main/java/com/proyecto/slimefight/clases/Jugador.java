package com.proyecto.slimefight.clases;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.proyecto.slimefight.actores.Slime;

import java.util.ArrayList;

public class Jugador {
    // TODO en el futuro debe contener su hilo cliente o algo asi ns

    private final int id;
    private final String nombre;
    private ArrayList<Punto> puntos;

    private Sprite ojosSlime;
    private Sprite bocaSlime;
    private Color colorSlime;

    private Slime slime;

    public Jugador(int id, String nombre) { this.id = id; this.nombre = nombre; puntos = new ArrayList<>(); }

    public void sumarPuntos(Punto p) {
        puntos.add(p);
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public ArrayList<Punto> getPuntos() { return puntos; }
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
