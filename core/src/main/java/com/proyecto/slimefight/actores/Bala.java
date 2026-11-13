package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
    private Sprite textura;
    private final float direccionX, direccionY;

    public int dano;
    public float knockback;
    private float tiempoDeVida;

    private final Body cuerpo;
    private final Slime dueno;      // quien disparó (no se golpea a sí mismo)
    private boolean impactada = false;
    private Slime objetivo = null;  // null = impactó terreno

    private boolean rotar = false;
    private float escalaExtraW = 0;
    private float escalaExtraH = 0;

    // Bala
    public Bala(World mundo, Slime dueno, Texture texture, float x, float y, float dirX, float scaleX, float scaleY, float velocidad, int dano, float knockback, float rango, float dispersion) {
        // textura
        if (textura == null) textura = new Sprite(texture);
        this.dueno = dueno;

        // Tamaño y pos
        setPosition(x, y);
        setSize(scaleX, scaleY);

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
        fd.filter.categoryBits = Constantes.CATEGORIA_BALA;
        fd.filter.maskBits = (Constantes.CATEGORIA_TERRENO | Constantes.CATEGORIA_SLIME | Constantes.CATEGORIA_CAJAS);
        cuerpo.createFixture(fd);
        figura.dispose();

        cuerpo.setUserData(this);

        // le stats
        cuerpo.setLinearVelocity(direccionX * velocidad / Constantes.PPM, direccionY * velocidad / Constantes.PPM);
        this.dano = dano;
        this.knockback = knockback;
        tiempoDeVida = rango;
    }

    public void setPropiedades(boolean rotar, float escalaTexturaX, float escalaTexturaY) {
        this.rotar = rotar;
        escalaExtraW = escalaTexturaX;
        escalaExtraH = escalaTexturaY;
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


        if (rotar) {
            textura.rotate(500 * delta);
        }

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
        textura.setPosition(getX(), getY());
        textura.setSize(getWidth() + escalaExtraW, getHeight() + escalaExtraH);
        textura.setOriginCenter();
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
        textura.draw(batch);
        batch.setColor(oldColor);
    }

    public boolean dirDerecha() {
        return direccionX > 0;
    }

    public Slime getDueno() {
        return dueno;
    }
}
