package com.proyecto.slimefight.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.proyecto.slimefight.actores.Slime;
import com.proyecto.slimefight.clases.Jugador;
import com.proyecto.slimefight.clases.Punto;
import utiles.Utils;

import java.util.ArrayList;

public class MostradorPuntaje {
    private Stage stage;
    private ArrayList<Slime> slimes;
    private final ArrayList<Jugador> jugadores;
    private final ArrayList<Label> etiquetas = new ArrayList<>();
    private final ArrayList<Container<Label>> contenedores = new ArrayList<>();
    private ArrayList<Punto> puntosPrevios;

    public MostradorPuntaje(ArrayList<Jugador> jugadores) {
        this.jugadores = jugadores;
        actualizarSlimes();
        stage = new Stage(new ScreenViewport());

        Table tabla = new Table();
        tabla.setFillParent(true);
        stage.addActor(tabla);

        for (Slime s : slimes) {
            IconoSlime icono = new IconoSlime(s.getTexturaCuerpo(), s.getOjos(), s.getBoca(), s.getColor(), 48);
            Table jugador = new Table();
            jugador.add(icono).size(48).padRight(10);
            tabla.add(jugador).expandY().row();
        }

    }

    public void update(float delta) {
        stage.act(delta);
    }

    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    public void draw() {
        stage.getViewport().apply();
        stage.draw();
    }

    public void actualizarSlimes() {
        slimes = new ArrayList<>();
        for (Jugador j : jugadores) {
            slimes.add(j.getSlime());
        }
    }

    public void dispose() {
        stage.dispose();
    }

}
