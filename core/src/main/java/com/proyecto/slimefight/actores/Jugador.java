package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.TomarInput;
import com.badlogic.gdx.math.Rectangle;

public class Jugador extends Actor {

    private Rectangle hitbox;
    private float moveX;
    private float moveY;

    private float velocidad = 100;

    public Jugador() {
        this.setSize(50, 50);
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
        moveX = ((input.derecha ? 1 : 0) - (input.izquierda ? 1 : 0)) * velocidad;
        moveY = ((input.arriba ? 1 : 0) - (input.abajo ? 1 : 0)) * velocidad;

        setX(getX() + moveX * delta);
        setY(getY() + moveY * delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
    }

}
