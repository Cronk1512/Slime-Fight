package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Config;
import utiles.TomarInput;
import com.badlogic.gdx.math.Rectangle;

import static utiles.Utils.Abs;

public class Jugador extends Actor {

    private Rectangle hitbox;
    private float width = 50;
    private float height = 50;
    private float moveX;
    private float moveY;

    private float velocidad = 300;
    private float poderSalto = 300;

    public Jugador() {
        this.setSize(width, height);
        hitbox = new Rectangle(getX(), getY(), getScaleX(), getScaleY());
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
    }

}
