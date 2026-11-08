package com.proyecto.slimefight.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class IconoSlime extends Actor {
    private static final float REBOTE_IDLE = 0.06f;

    private final TextureRegion cuerpo;
    private final Sprite ojos;
    private final Sprite boca;
    private float timer = 0;

    public IconoSlime(Texture texCuerpo, Sprite ojos, Sprite boca, Color color, float tam) {
        cuerpo = new TextureRegion(texCuerpo);
        // Copias: así no se pisa la posición/flip de los sprites del slime real
        this.ojos = new Sprite(ojos);
        this.boca = new Sprite(boca);
        this.ojos.setFlip(false, false);
        this.boca.setFlip(false, false);
        setColor(new Color(color));
        setSize(tam, tam);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timer += delta * 12f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // simular rebote
        float s = MathUtils.sin(timer) * REBOTE_IDLE;
        float escX = 1f + s;
        float escY = 1f - s;

        float w = getWidth() * escX;
        float h = getHeight() * escY;
        float x = getX() + (getWidth() - w) / 2f; // centrado, base anclada abajo
        float y = getY();

        Color viejo = new Color(batch.getColor());
        batch.setColor(getColor());
        batch.draw(cuerpo, x, y, w, h);

        // El slime real usa un offset de 5px sobre un cuerpo de 32px
        float offsetX = 5f / 32f * getWidth();
        ojos.setBounds(x + offsetX, y, getWidth(), h);
        boca.setBounds(x + offsetX, y, getWidth(), h);
        ojos.draw(batch);
        boca.draw(batch);
        batch.setColor(viejo);
    }
}

