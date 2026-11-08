package com.proyecto.slimefight.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.proyecto.slimefight.actores.Slime;
import utiles.Utils;

import java.util.ArrayList;

public class Hud {
    private final Stage stage;
    private final ArrayList<Slime> slimes;
    private final ArrayList<Label> etiquetas = new ArrayList<>();
    private final ArrayList<Container<Label>> contenedores = new ArrayList<>();
    BitmapFont fuente;

    public Hud(ArrayList<Slime> slimes) {
        this.slimes = slimes;
        stage = new Stage(new ScreenViewport());

        FreeTypeFontGenerator generador = new FreeTypeFontGenerator(Utils.obtenerRutaInterna("fuentes/Daydream DEMO.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 24;
        p.color = Color.WHITE;
        p.borderWidth = 2;
        p.borderColor = Color.BLACK;
        p.minFilter = Texture.TextureFilter.Nearest;
        p.magFilter = Texture.TextureFilter.Nearest;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúñÁÉÍÓÚÑ¡¿";
        BitmapFont fuente = generador.generateFont(p);
        generador.dispose();

        // TODO conseguir (o hacer) un mejor font (osea este esta buenisimo pero no tiene "%")

        Table tabla = new Table();
        tabla.setFillParent(true);
        tabla.bottom().pad(20);
        stage.addActor(tabla);

        for (Slime s : slimes) {
            IconoSlime icono = new IconoSlime(s.getTexturaCuerpo(), s.getOjos(), s.getBoca(), s.getColor(), 48);
            Label.LabelStyle estilo = new Label.LabelStyle(fuente, s.getColor()); // mismo color que el slime
            Label l = new Label("", estilo);
            etiquetas.add(l);

            Container<Label> cont = new Container<>(l); //Labels de % en un container para rotasao
            cont.setTransform(true);
            cont.setOrigin(Align.bottomLeft);
            cont.setRotation(25);
            contenedores.add(cont);

            Table jugador = new Table();
            jugador.add(icono).size(48).padRight(10);
            jugador.add(cont).width(100).height(40);
            tabla.add(jugador).expandX();   // reparte los jugadores a lo ancho
        }
    }

    public void update(float delta) {
        for (int i = 0; i < slimes.size(); i++) {
            Slime s = slimes.get(i);
            etiquetas.get(i).setText((int) s.getPorcentajeVida() + "%");
            contenedores.get(i).setOrigin(Align.bottomLeft);
        }
        stage.act(delta);
    }

    public void draw() {
        stage.getViewport().apply();
        stage.draw();
    }

    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    public void dispose() {
        stage.dispose();
        fuente.dispose();
    }

    public Stage getStage() {
        return stage;
    }
}
