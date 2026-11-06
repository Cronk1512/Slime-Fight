package com.proyecto.slimefight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.proyecto.slimefight.pantallas.Pantalla;
import com.proyecto.slimefight.pantallas.PantallaJuego;
import com.proyecto.slimefight.pantallas.PantallaMenuPrincipal;
import utiles.Config;

public class Main extends Game {

    public SpriteBatch spriteBatch;
    public static ShapeRenderer shapeR;
    private int ancho, alto;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        shapeR = new ShapeRenderer();
        ancho = Config.ancho;
        alto = Config.alto;
        // La primera pantalla que ve el jugador
        setScreen(new PantallaJuego(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeR.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0,0,ancho,alto);
    }
}
