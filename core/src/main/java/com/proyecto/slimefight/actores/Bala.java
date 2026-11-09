package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Constantes;
import utiles.Utils;

import java.util.Random;

public class Bala extends Actor {
    private static Texture pixel;
    public Rectangle hitbox;
    private final float direccionX, direccionY;

    public int dano;
    public float knockback;
    private float velocidad;
    private float tiempoDeVida;

    private final Body cuerpo;
    private final Slime dueno;      // quien disparó (no se golpea a sí mismo)
    private boolean impactada = false;
    private Slime objetivo = null;  // null = impactó terreno


    // Bala
    public Bala(World mundo, Slime dueno, float x, float y, float dirX, float scaleX, float velocidad, int dano, float knockback, float rango, float dispersion) {
        // textura
        if (pixel == null) pixel = new Texture(Utils.obtenerRutaInterna("pixel_blanco.png"));
        this.dueno = dueno;

        // Tamaño y pos
        setPosition(x, y);
        setSize(scaleX, 2);
        hitbox = new Rectangle(x, y, getWidth(), getHeight());

        // Direccion
        Random rng = new Random();
        float disp = (float) rng.nextGaussian() * dispersion;
        Vector2 vector = new Vector2(MathUtils.clamp(dirX, -1f, 1f), MathUtils.clamp(disp, -1f, 1f)).nor();
        direccionX = vector.x;
        direccionY = vector.y;

        // Cuerpo
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set((x + getWidth() / 2f) / Constantes.PPM, (y + getHeight() / 2f) / Constantes.PPM);
        bd.gravityScale = 0;
        bd.fixedRotation = true;
        bd.bullet = true;
        cuerpo = mundo.createBody(bd);

        PolygonShape figura = new PolygonShape();
        figura.setAsBox(getWidth() / 2f / Constantes.PPM, getHeight() / 2f / Constantes.PPM);
        FixtureDef fd = new FixtureDef();
        fd.shape = figura;
        fd.isSensor = true;
        cuerpo.createFixture(fd);
        figura.dispose();

        cuerpo.setUserData(this);

        // le stats
        cuerpo.setLinearVelocity(direccionX * velocidad / Constantes.PPM, direccionY * velocidad / Constantes.PPM);
        this.dano = dano;
        this.knockback = knockback;
        tiempoDeVida = rango;
    }

    public void impactar(Slime objetivo) {
        if (impactada) return;      // evita doble impacto en el mismo step del world
        impactada = true;
        this.objetivo = objetivo;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        tiempoDeVida -= delta * 5f;

        if (impactada) {
            if (objetivo != null) objetivo.recibirDano(this);
            destruir();
            return;
        }
        if (tiempoDeVida <= 0) {
            destruir();
            return;
        }

        setPosition(cuerpo.getPosition().x * Constantes.PPM - getWidth() / 2f, cuerpo.getPosition().y * Constantes.PPM - getHeight() / 2f);
        hitbox.set(getX(), getY(), getWidth(), getHeight());
    }

    private void destruir() {
        cuerpo.getWorld().destroyBody(cuerpo);
        remove();
    }

    public int getDuenoId() {
        return dueno.getId();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color oldColor = new Color(batch.getColor());
        batch.setColor(Color.YELLOW);
        batch.draw(pixel, getX(), getY(), getWidth(), 2);
        batch.setColor(oldColor);
    }

    public boolean dirDerecha() {
        return direccionX > 0;
    }

    public Slime getDueno() {
        return dueno;
    }
}
