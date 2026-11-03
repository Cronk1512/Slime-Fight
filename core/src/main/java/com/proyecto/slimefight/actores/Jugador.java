package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Config;
import utiles.TomarInput;
import com.badlogic.gdx.math.Rectangle;
import utiles.Utils;

import static utiles.Utils.Abs;

public class Jugador extends Actor {

    private Sprite ojos;
    private Sprite boca;
    private Rectangle hitbox;
    private TextureRegion pixelBlanco;

    private float width = 50;
    private float height = 50;
    private float moveX;
    private float moveY;

    private float velocidad = 300;
    private float poderSalto = 300;

    public Jugador(Sprite ojos, Sprite boca) {
        this.setSize(width, height);
        hitbox = new Rectangle(getX(), getY(), getScaleX(), getScaleY());

        this.ojos = ojos;
        this.boca = boca;
        pixelBlanco = new TextureRegion(new Texture(Utils.obtenerRutaInterna("pixel_blanco.png")));

        setColor(Color.RED);
    }

    /*
    @Override
    public void act (float delta) {

    }
    */

    public void update(float delta, float posicion, TomarInput input){
        moverPersonaje(delta, posicion, input);
    }

    private void moverPersonaje(float delta, float posicion, TomarInput input) {
        float gravedad = Config.gravedad;

        // Movimiento horizontal
        moveX = ((input.derecha ? 1 : 0) - (input.izquierda ? 1 : 0)) * velocidad;
        setX(getX() + moveX * delta);

        // Movimiento vertical
        boolean puedeSaltar = getY() <= 0; // TODO en vez de 0 sería el piso (aunque tambien deberia poder saltar al tocar una pared)
        if (getY() > 0 && moveY > 0) moveY -= gravedad;
        if (getY() > 0 && moveY <= 0) moveY -= gravedad * 2.5f;
        if (puedeSaltar && input.arriba) moveY = poderSalto;
        if (!input.arriba && moveY > 0) moveY = 0;
        if (getY() < 0) {
            moveY = 0;
            setY(0);
        }
        setY(getY() + moveY * delta);

        // TODO Aplastar si se mueve horizontalmente O al aterrizar (usando tweens)
        // TODO Estirar si se mueve verticalmente O si se choca con una pared (usando tweens)

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Pinta el slime de un color (tiñiendo y estirando un pixel blanco)
        float colorBatch = batch.getPackedColor();

        Color colorActor = getColor();
        batch.setColor(colorActor.r, colorActor.g, colorActor.b, colorActor.a);
        batch.draw(pixelBlanco, getX(), getY(), getWidth(), getHeight());

        ojos.setPosition(getX()+5, getY());
        ojos.setSize(getWidth(), getHeight());
        boca.setPosition(getX()+5, getY());
        boca.setSize(getWidth(), getHeight());

        ojos.draw(batch);
        boca.draw(batch);
        batch.setPackedColor(colorBatch);
    }

}
