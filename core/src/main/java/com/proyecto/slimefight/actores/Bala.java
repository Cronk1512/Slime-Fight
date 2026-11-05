package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Utils;

public class Bala extends Actor {
    private final float direccionX;
    private final float direccionY;
    private float tiempoDeVida;
    private Texture pixel = new Texture(Utils.obtenerRutaInterna("pixel_blanco.png"));
    public Rectangle hitbox;

    public float dano;
    public float knockback;
    private float velocidad;

    public Bala(float x, float y, float dirX, float dirY, float scaleX, float velocidad, float dano, float knockback, float rango) {
        // Tamaño y pos
        setX(x);
        setY(y);
        setWidth(scaleX);
        setHeight(5);
        hitbox = new Rectangle(getX(),getY(),getWidth(),getHeight());

        // Direccion
        Vector2 vector = new Vector2(MathUtils.clamp(dirX, -1f, 1f), MathUtils.clamp(dirY, -1f, 1f)).nor();
        direccionX = vector.x;
        direccionY = vector.y;

        // le stats
        this.velocidad = velocidad;
        this.dano = dano;
        this.knockback = knockback;
        tiempoDeVida = rango;
    }

    public Bala(float x, float y, float dirX, float dirY, float scaleX, float velocidad, float dano, float knockback) {
        // Tamaño y pos
        setX(x);
        setY(y);
        setWidth(scaleX);
        setHeight(5);

        // Direccion
        Vector2 vector = new Vector2(MathUtils.clamp(dirX, -1f, 1f), MathUtils.clamp(dirY, -1f, 1f)).nor();
        direccionX = vector.x;
        direccionY = vector.y;

        // le stats
        this.velocidad = velocidad;
        this.dano = dano;
        this.knockback = knockback;
        tiempoDeVida = 25;
    }

    public boolean deleteCheck() {
        return tiempoDeVida <= 0;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        update(delta);
    }

    public void update(float delta) {
        tiempoDeVida -= delta * 5f;
        if (deleteCheck()) this.remove();
        moverBala(delta);
    }

    private void moverBala(float delta) {
        setX(getX() + direccionX * velocidad * delta);
        setY(getY() + direccionY * velocidad * delta);
        hitbox = new Rectangle(getX(),getY(),getWidth(),getHeight());
    }

    public boolean dirDerecha() {
        return direccionX > 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(pixel, getX(), getY(), getWidth(), getHeight());
    }

}
