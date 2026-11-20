package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import utiles.Constantes;
import utiles.Utils;

import java.util.ArrayList;
import java.util.Random;

// Caja que contiene un arma aleatoria
public class Caja extends Actor {

    private static final Random RNG = new Random();

    private Body cuerpo;
    private int contactosPiso = 0;
    private Texture textura;
    private Sprite sprite;
    private ArrayList<Arma> armasDisponibles;
    private Stage stage;
    private Slime recogidaPor;
    private boolean destruida = false;

    private int id;

    public Caja(float x, float y, World mundo, Stage stage, ArrayList<Arma> armas) {
        textura = new Texture(Utils.obtenerRutaInterna("caja.png"));
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sprite = new Sprite(textura);
        this.stage = stage;
        armasDisponibles = armas;
        setSize(25, 25);
        setPosition(x, y);

        // Cuerpo
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set((x + getWidth() / 2f) / Constantes.PPM, (y + getHeight() / 2f) / Constantes.PPM);
        cuerpo = mundo.createBody(bd);

        PolygonShape figura = new PolygonShape();
        PolygonShape detector = new PolygonShape();
        figura.setAsBox(getWidth() / 2f / Constantes.PPM, getHeight() / 2f / Constantes.PPM);
        detector.setAsBox(getWidth() / 2f / Constantes.PPM, getHeight() / 2f / Constantes.PPM);

        FixtureDef principal = new FixtureDef();
        FixtureDef sensor = new FixtureDef();
        principal.shape = figura;
        principal.filter.categoryBits = Constantes.CATEGORIA_CAJAS;
        principal.filter.maskBits = (Constantes.CATEGORIA_TERRENO | Constantes.CATEGORIA_CAJAS | Constantes.CATEGORIA_BALA);
        sensor.shape = detector;
        sensor.isSensor = true;
        sensor.filter.categoryBits = Constantes.CATEGORIA_CAJAS;
        sensor.filter.maskBits = Constantes.CATEGORIA_SLIME;
        cuerpo.createFixture(principal);
        cuerpo.createFixture(sensor);
        cuerpo.setUserData(this);
        figura.dispose();
        detector.dispose();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (destruida || cuerpo == null) return;
        setPosition(cuerpo.getPosition().x * Constantes.PPM - getWidth() / 2f, cuerpo.getPosition().y * Constantes.PPM - getHeight() / 2f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (destruida || sprite == null) return;
        sprite.setPosition(getX(), getY());
        sprite.setSize(getWidth(), getHeight());
        sprite.draw(batch);
    }

    public void sincronizar(float x, float y, float umbral) {
        if (destruida || cuerpo == null) return;
        float cx = x + getWidth() / 2f, cy = y + getHeight() / 2f;
        float dx = cx - cuerpo.getPosition().x * Constantes.PPM;
        float dy = cy - cuerpo.getPosition().y * Constantes.PPM;
        if (dx * dx + dy * dy > umbral * umbral) cuerpo.setTransform(cx / Constantes.PPM, cy / Constantes.PPM, 0);
    }

    public void generarArma(Slime slime) {
        if (destruida || slime == null || armasDisponibles == null || armasDisponibles.isEmpty()) {
            destruir();
            return;
        }

        // Elige un arma al azar de las cargadas desde AyudanteArmas / JSON
        Arma plantilla = armasDisponibles.get(RNG.nextInt(armasDisponibles.size()));
        Arma arma = new Arma(plantilla);

        if (slime.getArma() != null) slime.getArma().destruir();
        slime.setArma(arma);
        if (stage != null) stage.addActor(arma);
        destruir();
    }

    public void empujar(float x, float y) {
        if (destruida || cuerpo == null) return;
        cuerpo.setLinearVelocity(cuerpo.getLinearVelocity().x + x, cuerpo.getLinearVelocity().y + y);
    }

    public void destruir() {
        if (destruida) return;
        destruida = true;
        if (cuerpo != null && cuerpo.getWorld() != null) {
            cuerpo.getWorld().destroyBody(cuerpo);
            cuerpo = null;
        }
        if (textura != null) {
            textura.dispose();
            textura = null;
        }
        remove();
    }

    public void marcarRecogida(Slime s) {
        if (recogidaPor == null) recogidaPor = s;
    }

    public Slime getRecogidaPor() { return recogidaPor; }
    public boolean isDestruida() { return destruida; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
