package com.proyecto.slimefight;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.proyecto.slimefight.pantallas.MenuPrincipal;
import utiles.Config;
import utiles.Utils;

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
        setScreen(new MenuPrincipal(this));
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
