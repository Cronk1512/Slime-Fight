package com.proyecto.slimefight.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import utiles.Timer;
import utiles.Utils;

public class CuentaAtras extends Actor {
    private static final float DURACION_FIGHT = 1f;

    private final Texture[] texturas = new Texture[4];
    private final TextureRegion[] regiones = new TextureRegion[4]; // 3, 2, 1, fight

    private float tiempo = 0f;
    private boolean cuentaAtrasFinalizada = false;

    public CuentaAtras(float tam) {
        String[] rutas = {"3", "2", "1", "fight"};
        for (int i = 0; i < 4; i++) {
            texturas[i] = new Texture(Utils.obtenerRutaInterna("intro/" + rutas[i] + "_placeholder.png"));
            texturas[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            regiones[i] = new TextureRegion(texturas[i]);
        }
        setSize(tam, tam);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        tiempo += delta * 2;
        if (tiempo >= 3f) cuentaAtrasFinalizada = true;
        if (tiempo >= 3f + DURACION_FIGHT) remove();
    }


    float oldWidth;
    @Override
    public void draw(Batch batch, float parentAlpha) {
        int paso = Math.min((int) tiempo, 3);                 // 0,1,2 = 3,2,1 | 3 = fight
        float frac = tiempo - (int) tiempo;                   // 0..1 dentro de cada segundo
        if (paso == 3) frac = Math.min(tiempo - 3f, 1f);
        if (paso == 0) oldWidth = getWidth();
        if (paso == 3) setWidth(oldWidth*3);

        float escala = 1f + 0.6f * (1f - Interpolation.pow2Out.apply(frac));
        float w = getWidth() * escala;
        float h = getHeight() * escala;

        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(regiones[paso], getX() - w / 2f, getY() - h / 2f, w, h);

    }

    public boolean isCuentaAtrasFinalizada() {
        return cuentaAtrasFinalizada;
    }

    public void dispose() {
        for (Texture t : texturas) t.dispose();
    }
}
