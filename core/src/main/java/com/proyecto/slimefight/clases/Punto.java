package com.proyecto.slimefight.clases;

import com.badlogic.gdx.graphics.Color;

// Clase punto para mostrar el orden y fuente de los puntos con su respectivo color
public class Punto {
    private final String nombre;
    private final int valor;
    private final Color color;

    public Punto(String nombre, int valor, Color color) {
        this.nombre = nombre;
        this.valor = valor;
        this.color = new Color(color);
    }

    public Punto(Punto p) {
        this.nombre = p.nombre;
        this.valor = p.valor;
        this.color = new Color(p.color);
    }

    public String getNombre() {
        return nombre;
    }

    public int getValor() {
        return valor;
    }

    public Color getColor() {
        return color;
    }
}
