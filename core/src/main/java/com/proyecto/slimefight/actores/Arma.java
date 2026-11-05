package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Arma extends Actor {
    private Sprite sprite;
    boolean volteado = false;
    private float offX;
    private float offY;

    private int dano;
    private float knockback;
    private float rango;
    private float cooldown;
    private float durabilidad;

    private float timerCooldown = 0;

    public Arma(float offsetX, float offsetY, float width, float height, Texture textura, int dano, float knockback, float rango, float cooldown, float durabilidad) {
        textura.setFilter(TextureFilter.Nearest,TextureFilter.Nearest);
        sprite = new Sprite(textura);

        setWidth(width);
        setHeight(height);

        offX = offsetX;
        offY = offsetY;

        this.dano = dano;
        this.knockback = knockback;
        this.rango = rango;
        this.cooldown = cooldown;
    }

    public void update(float delta, float x, float y, boolean volteado) {
        setX(x + offX);
        setY(y + offY);

        if (this.volteado != volteado) {
            darVuelta();
        }

        if (timerCooldown > 0) timerCooldown -= delta * 16f;
    }

    public void disparar() {
        if (timerCooldown <= 0) {
            timerCooldown = cooldown;
            Bala bala;
            Vector2 puntoDeFuego;
            durabilidad -= 1;
            if (!volteado) {
                puntoDeFuego = new Vector2(getX()+3, getY()+getHeight()/2);
                bala = new Bala(puntoDeFuego.x, puntoDeFuego.y, 1, 0, 7, 1000, dano, knockback, rango);
            } else {
                puntoDeFuego = new Vector2(getX()+3, getY()+getHeight()/2);
                bala = new Bala(puntoDeFuego.x, puntoDeFuego.y, -1, 0, 7, 1000, dano, knockback, rango);
            }
            this.getStage().addActor(bala);
        }
    }

    public void darVuelta() {
        volteado = !volteado;
        sprite.flip(true, false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(sprite, getX()-getWidth()/2, getY()-getHeight()/2, getWidth(), getHeight());
    }
}
