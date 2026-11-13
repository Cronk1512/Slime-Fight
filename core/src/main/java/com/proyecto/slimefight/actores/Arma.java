package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Constantes;

public class Arma extends Actor {
    private final Sprite sprite;
    boolean volteado = false;
    private float offX;
    private float offY;

    private String nombre;
    private int dano;
    private float knockback;
    private float rango;
    private float cooldown;
    private int durabilidad;
    private float dispersion;
    private float anchoBala;
    private float altoBala;
    private float cantBalas;
    private Slime dueno;
    private World mundo;
    private Texture texturaBala;
    private boolean estaRoto = false;

    private float timerCooldown = 0;

    public Arma(String nombre, World mundo, float offsetX, float offsetY, float width, float height, Texture textura,
                int dano, float knockback, float rango, float dispersion, float anchoBala, float altoBala, Texture texturaBala, float cooldown, int durabilidad, int cantBalas) {
        textura.setFilter(TextureFilter.Nearest,TextureFilter.Nearest);
        sprite = new Sprite(textura);

        setWidth(width);
        setHeight(height);
        sprite.setSize(width, height);

        offX = offsetX;
        offY = offsetY;

        this.nombre = nombre;
        this.cooldown = cooldown;
        this.durabilidad = durabilidad;
        if (cantBalas==0) cantBalas = 1;
        this.cantBalas = Math.abs(cantBalas);

        this.texturaBala = texturaBala;
        this.dispersion = dispersion;
        this.anchoBala = Math.max(5f, Math.abs(anchoBala));
        this.altoBala = Math.max(5f, Math.abs(altoBala));
        this.dano = dano;
        this.knockback = knockback;
        this.rango = rango;

        this.mundo = mundo;
    }

    public Arma(Arma arma) {
        sprite = new Sprite(arma.sprite);

        setWidth(arma.getWidth());
        setHeight(arma.getHeight());
        sprite.setSize(arma.getWidth(), arma.getHeight());

        offX = arma.offX;
        offY = arma.offY;

        this.nombre = arma.nombre;
        this.cooldown = arma.cooldown;
        this.durabilidad = arma.durabilidad;
        cantBalas = arma.cantBalas;

        this.texturaBala = arma.texturaBala;
        this.dano = arma.dano;
        this.knockback = arma.knockback;
        this.rango = arma.rango;
        this.dispersion = arma.dispersion;
        this.anchoBala = Math.max(5f, Math.abs(arma.anchoBala));
        this.altoBala = Math.max(5f, Math.abs(arma.altoBala));

        this.mundo = arma.mundo;
    }

    public void update(float delta, float x, float y, boolean volteado, Slime dueno) {
        setX(x + offX);
        setY(y + offY);

        sprite.setPosition(getX()-getWidth()/2, getY()-getHeight()/2);
        sprite.setOriginCenter();

        this.dueno = dueno;

        if (this.volteado != volteado) {
            darVuelta();
        }

        if (timerCooldown > 0) {
            timerCooldown -= delta * 16f;
            sprite.setRotation(MathUtils.lerp(sprite.getRotation(), 0, delta * 16f));
        } else {
            sprite.setRotation(0);
        }
    }

    public void disparar() {
        if (timerCooldown <= 0) {
            timerCooldown = cooldown;
            Bala bala;
            Vector2 puntoDeFuego;

            if (durabilidad > 0) durabilidad -= 1;
            if (durabilidad == 0) estaRoto = true;

            if (!volteado) { // rotacion (rota aunque este roto)
                sprite.setRotation(40);
            } else {
                sprite.setRotation(-40);
            }

            if (!volteado && !estaRoto) { // disparacion
                puntoDeFuego = new Vector2(getX()+3, getY()+getHeight()/2);
                for (int i = 0; i < cantBalas; i++) {
                    bala = new Bala(mundo, dueno, texturaBala, puntoDeFuego.x, puntoDeFuego.y, 1,anchoBala, altoBala, 500, dano, knockback, rango, dispersion);
                    this.getStage().addActor(bala);
                }
            } else if (!estaRoto) {
                puntoDeFuego = new Vector2(getX()+3, getY()+getHeight()/2);
                for (int i = 0; i < cantBalas; i++) {
                    bala = new Bala(mundo, dueno, texturaBala, puntoDeFuego.x, puntoDeFuego.y, -1, anchoBala, altoBala, 500, dano, knockback, rango, dispersion);
                    this.getStage().addActor(bala);
                }
            }
        }
    }

    public void lanzarArma() {
        Bala bala;
        Vector2 puntoDeFuego;

        if (!volteado) { // lanza le arma (lo mismo que disparar basicamente)
            puntoDeFuego = new Vector2(getX()+3, getY());
            for (int i = 0; i < cantBalas; i++) {
                bala = new Bala(mundo, dueno, sprite.getTexture(), puntoDeFuego.x, puntoDeFuego.y, 1, 20, 20, 650, 7, 25, 25, 0);
                bala.setPropiedades(true, sprite.getWidth()/2, sprite.getHeight()/2);
                this.getStage().addActor(bala);
            }
        } else {
            puntoDeFuego = new Vector2(getX()+3, getY());
            for (int i = 0; i < cantBalas; i++) {
                bala = new Bala(mundo, dueno, sprite.getTexture(), puntoDeFuego.x, puntoDeFuego.y, -1, 20, 20, 650, 7, 25, 25, 0);
                bala.setPropiedades(true, sprite.getWidth()/2, sprite.getHeight()/2);
                this.getStage().addActor(bala);
            }
        }
        destruir();
    }

    public void destruir() {
        remove();
    }

    public void darVuelta() {
        volteado = !volteado;
        sprite.flip(true, false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.draw(batch);
        //batch.draw(sprite, getX()-getWidth()/2, getY()-getHeight()/2, getWidth(), getHeight());
    }

    public int getDurabilidad() {
        return durabilidad;
    }

    public boolean isEstaRoto() {
        return estaRoto;
    }
}
